import 'package:flutter/material.dart';
// ignore: depend_on_referenced_packages
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/favors/providers/upload_favor_state_notifier_provider.dart.dart';
import 'package:senefavores/state/location/providers/user_location_state_notifier_provider.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:senefavores/views/components/senefavores_image_and_title_and_profile.dart';
import 'package:senefavores/utils/logger.dart';
import 'package:senefavores/state/favors/providers/favor_acceptance_rate_provider.dart';

class PostFavorScreen extends HookConsumerWidget {
  const PostFavorScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentUser = ref.read(currentUserNotifierProvider);
    final titleController = useTextEditingController();
    final descriptionController = useTextEditingController();
    final rewardController = useTextEditingController();
    final selectedCategory = useState<String?>(null);
    final averageAcceptanceTimeAsync = ref.watch(
      favorAverageAcceptanceTimeProvider(selectedCategory.value ?? ''),
    );

    // Hooks to listen to text field changes.
    final titleValue = useValueListenable(titleController);
    final descriptionValue = useValueListenable(descriptionController);

    return SafeArea(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          const SenefavoresImageAndTitleAndProfile(),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 8.0),
            child: Text(
              "Crear Favor",
              style: AppTextStyles.oswaldTitle,
            ),
          ),
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Title Field with a live counter and a 60 character limit.
                  Row(
                    children: [
                      Text("Título:", style: AppTextStyles.oswaldSubtitle),
                      const SizedBox(width: 10),
                      Expanded(
                        child: TextField(
                          controller: titleController,
                          cursorColor: Colors.black,
                          maxLength: 60,
                          decoration: customInputDecoration(
                            counterText: "${titleValue.text.length}/60",
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 15),
                  // Description Field with a live counter and a 250 character limit.
                  Text("Descripción:", style: AppTextStyles.oswaldSubtitle),
                  const SizedBox(height: 5),
                  TextField(
                    controller: descriptionController,
                    cursorColor: Colors.black,
                    maxLines: 4,
                    maxLength: 250,
                    decoration: customInputDecoration(
                      hintText: "Descripción (máx. 250 caracteres)",
                      counterText: "${descriptionValue.text.length}/250",
                    ),
                  ),
                  const SizedBox(height: 15),
                  // Reward Field.
                  Row(
                    children: [
                      Text("Recompensa:", style: AppTextStyles.oswaldSubtitle),
                      const SizedBox(width: 10),
                      Expanded(
                        child: TextField(
                          controller: rewardController,
                          cursorColor: Colors.black,
                          keyboardType: TextInputType.number,
                          decoration: customInputDecoration(
                              hintText: r"$100 - $1.000.000"),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 15),
                  // Category Selection.
                  Text("Categoría:", style: AppTextStyles.oswaldSubtitle),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.start,
                    children: [
                      _categoryButton(
                        "Favor",
                        AppColors.lightRed,
                        selectedCategory,
                      ),
                      const SizedBox(width: 10),
                      _categoryButton(
                        "Compra",
                        AppColors.lightSkyBlue,
                        selectedCategory,
                      ),
                      const SizedBox(width: 10),
                      _categoryButton(
                        "Tutoría",
                        AppColors.orangeWeb,
                        selectedCategory,
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  if (selectedCategory.value != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 10),
                      child: averageAcceptanceTimeAsync.when(
                        data: (time) {
                          return Text(
                            "Tiempo promedio de aceptación: ${time.toStringAsFixed(2)} minutos",
                            style: AppTextStyles.oswaldSubtitle,
                          );
                        },
                        loading: () => const CircularProgressIndicator(),
                        error: (error, stackTrace) =>
                            Text("Error al cargar el tiempo promedio"),
                      ),
                    ),
                  const SizedBox(height: 30),
                ],
              ),
            ),
          ),
          // Publish Button.
          Padding(
            padding: const EdgeInsets.all(20),
            child: SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () async {
                  final start = DateTime.now();

                  final title = titleController.text.trim();
                  final description = descriptionController.text.trim();
                  final rewardText = rewardController.text.trim();

                  if (title.isEmpty ||
                      description.isEmpty ||
                      rewardText.isEmpty ||
                      selectedCategory.value == null) {
                    ref.read(snackbarProvider).showSnackbar(
                          "Por favor llena todos los campos",
                          isError: true,
                        );
                    return;
                  }

                  if (title.length > 60) {
                    ref.read(snackbarProvider).showSnackbar(
                          "El título no puede exceder 60 caracteres",
                          isError: true,
                        );
                    return;
                  }

                  if (!RegExp(r'[A-Za-z0-9]').hasMatch(title)) {
                    ref.read(snackbarProvider).showSnackbar(
                          "El título debe contener al menos un carácter alfanumérico",
                          isError: true,
                        );
                    return;
                  }

                  // Parse and validate the reward value.
                  final rewardValue = int.tryParse(rewardText);
                  if (rewardValue == null ||
                      rewardValue < 100 ||
                      rewardValue > 1000000) {
                    ref.read(snackbarProvider).showSnackbar(
                          r"La recompensa debe estar entre $100 y $1.000.000",
                          isError: true,
                        );
                    return;
                  }

                  if (selectedCategory.value == "Favor" ||
                      selectedCategory.value == "Compra") {
                    final isNear = await ref
                        .read(userLocationProvider.notifier)
                        .isUserNearLocation();

                    if (!isNear) {
                      ref.read(snackbarProvider).showSnackbar(
                            "Debes estar cerca de la Universidad de los Andes para publicar un favor de tipo Favor o Compra",
                            isError: true,
                          );
                      return;
                    }
                  }

                  try {
                    final locationData = await ref
                        .read(userLocationProvider.notifier)
                        .getCurrentLocation();

                    final success = await ref
                        .read(uploadFavorStateNotifierProvider.notifier)
                        .uploadFavor(
                          favor: FavorModel(
                            id: '',
                            title: title,
                            description: description,
                            category: (selectedCategory.value ?? "favor")
                                .toLowerCase(),
                            reward: rewardValue,
                            createdAt: DateTime.now(),
                            requestUserId: currentUser!.id,
                            latitude: locationData?.latitude,
                            longitude: locationData?.longitude,
                            status: 'pending',
                          ),
                        );

                    final duration =
                        DateTime.now().difference(start).inMilliseconds;
                    await AppLogger.logResponseTime(
                      screen: 'PostFavorScreen',
                      responseTimeMs: duration,
                    );

                    if (success) {
                      titleController.clear();
                      descriptionController.clear();
                      rewardController.clear();
                      selectedCategory.value = null;
                      ref.read(snackbarProvider).showSnackbar(
                            "Favor publicado con éxito",
                            isError: false,
                          );
                    } else {
                      ref.read(snackbarProvider).showSnackbar(
                            "Error al publicar el favor",
                            isError: true,
                          );
                    }
                  } catch (e) {
                    await AppLogger.logCrash(
                      screen: 'PostFavorScreen',
                      crashInfo: e.toString(),
                    );

                    ref.read(snackbarProvider).showSnackbar(
                          "Error: $e",
                          isError: true,
                        );
                  }
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.mikadoYellow,
                  foregroundColor: Colors.black,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                    side: const BorderSide(color: Colors.black, width: 1),
                  ),
                  elevation: 0,
                  shadowColor: Colors.transparent,
                ),
                child: Text(
                  "Publicar",
                  style: AppTextStyles.oswaldTitle,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  InputDecoration customInputDecoration({
    String? hintText,
    String? counterText,
  }) {
    return InputDecoration(
      hintText: hintText,
      counterText: counterText,
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(20),
        borderSide: const BorderSide(
          color: Colors.grey,
          width: 4,
        ),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(20),
        borderSide: const BorderSide(
          color: Colors.black,
          width: 2,
        ),
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
    );
  }

  Widget _categoryButton(
    String text,
    Color color,
    ValueNotifier<String?> selectedCategory,
  ) {
    final isSelected = selectedCategory.value == text;
    return GestureDetector(
      onTap: () => selectedCategory.value = text,
      child: Chip(
        label: Text(text),
        backgroundColor: isSelected ? color : Colors.white,
        labelStyle: TextStyle(
          color: isSelected ? Colors.white : Colors.black,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }
}

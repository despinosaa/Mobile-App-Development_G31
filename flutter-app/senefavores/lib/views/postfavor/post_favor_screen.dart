import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
// ignore: depend_on_referenced_packages
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hive/hive.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import 'package:senefavores/core/constant.dart';
import 'package:senefavores/state/connectivity/connectivity_provider.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/favors/providers/category_text_provider.dart';
import 'package:senefavores/state/favors/providers/reward_text_provider.dart';
import 'package:senefavores/state/favors/providers/reward_value_provider.dart';
import 'package:senefavores/state/favors/providers/upload_favor_state_notifier_provider.dart.dart';
import 'package:senefavores/state/favors/providers/favor_acceptance_time_provider.dart';
import 'package:senefavores/state/favors/providers/no_response_favors_count_provider.dart';
import 'package:senefavores/state/location/providers/user_location_state_notifier_provider.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:senefavores/utils/logger.dart';
import 'package:senefavores/views/components/senefavores_image_and_title_and_profile.dart';
import 'package:senefavores/views/navigation/navigation_screen.dart';

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

    final pendingFavorsCountAsync = ref.watch(noResponseFavorsCountProvider);
    final connectivity = ref.watch(connectivityProvider).value;
    final isUploading = ref.watch(uploadFavorStateNotifierProvider);

    final titleValue = useValueListenable(titleController);
    final descriptionValue = useValueListenable(descriptionController);

    useEffect(() {
      final favorDraftBox = Hive.box('favor_drafts');
      if (connectivity != ConnectivityResult.none) {
        if (favorDraftBox.containsKey('draft')) {
          final cached = favorDraftBox.get('draft') as Map;

          titleController.text = cached['title'] ?? '';
          descriptionController.text = cached['description'] ?? '';
          rewardController.text = cached['reward'] ?? '';
          selectedCategory.value = cached['category'];

          favorDraftBox.delete('draft');
          SchedulerBinding.instance.addPostFrameCallback((_) {
            ref.read(snackbarProvider).showSnackbar(
                  "Se restauró un borrador guardado anteriormente",
                  isError: false,
                );
          });
        }
      }
      return null;
    }, []);

    useEffect(() {
      void rewardListener() {
        ref.read(rewardTextProvider.notifier).state = rewardController.text;
      }

      rewardController.addListener(rewardListener);
      return () => rewardController.removeListener(rewardListener);
    }, [rewardController]);

    useEffect(() {
      void categoryListener() {
        ref.read(categoryTextProvider.notifier).state =
            selectedCategory.value ?? '';
      }

      selectedCategory.addListener(categoryListener);
      return () => selectedCategory.removeListener(categoryListener);
    }, [selectedCategory]);

    final acceptanceProbability = ref.watch(acceptanceProbabilityProvider);

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
                  // Title Field
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
                  // Description Field
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
                  // Reward Field
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
                            hintText: r"$100 - $1.000.000",
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 15),
                  // Category Selection
                  Text("Categoría:", style: AppTextStyles.oswaldSubtitle),
                  Row(
                    children: [
                      _categoryButton(
                          "Favor", AppColors.lightRed, selectedCategory),
                      const SizedBox(width: 10),
                      _categoryButton(
                          "Compra", AppColors.lightSkyBlue, selectedCategory),
                      const SizedBox(width: 10),
                      _categoryButton(
                          "Tutoría", AppColors.orangeWeb, selectedCategory),
                    ],
                  ),
                  const SizedBox(height: 10),
                  // Average acceptance time
                  if (selectedCategory.value != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 10),
                      child: averageAcceptanceTimeAsync.when(
                        data: (time) => Text(
                          "Tiempo promedio de aceptación: ${time.toStringAsFixed(2)} minutos",
                          style: AppTextStyles.oswaldSubtitle,
                        ),
                        loading: () => const CircularProgressIndicator(),
                        error: (e, _) => Text(
                          "Error al cargar el tiempo promedio",
                          style: AppTextStyles.oswaldSubtitle
                              .copyWith(color: Colors.red),
                        ),
                      ),
                    ),
                  const SizedBox(height: 20),
                  // Pending favors count
                  pendingFavorsCountAsync.when(
                    data: (count) => Text(
                      "Favores sin aceptar hoy: $count",
                      style: AppTextStyles.oswaldSubtitle,
                    ),
                    loading: () => const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    ),
                    error: (e, _) => Text(
                      "Error al cargar conteo de favores",
                      style: AppTextStyles.oswaldSubtitle
                          .copyWith(color: Colors.red),
                    ),
                  ),
                  Text(
                    "Probabilidad de aceptacion: $acceptanceProbability%",
                    style: AppTextStyles.oswaldSubtitle,
                  ),
                  Text(
                    "Hora con mayor aceptacion: 2:32pm",
                    style: AppTextStyles.oswaldSubtitle,
                  ),

                  const SizedBox(height: 30),
                ],
              ),
            ),
          ),
          // Publish Button
          Padding(
            padding: const EdgeInsets.all(20),
            child: SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: isUploading
                    ? null
                    : () async {
                        final favorDraftBox = Hive.box('favor_drafts');

                        // Check connectivity
                        if (connectivity == ConnectivityResult.none) {
                          // Cache current input
                          favorDraftBox.put('draft', {
                            'title': titleController.text.trim(),
                            'description': descriptionController.text.trim(),
                            'reward': rewardController.text.trim(),
                            'category': selectedCategory.value,
                          });

                          ref.read(snackbarProvider).showSnackbar(
                                "No se puede publicar sin conexión a Internet. El favor fue guardado como borrador.",
                                isError: true,
                              );
                          return;
                        }

                        // START actual logic if online
                        final start = DateTime.now();
                        final title = titleController.text.trim();
                        final description = descriptionController.text.trim();
                        final rewardText = rewardController.text.trim();

                        // Validation
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

                        final rewardValue = int.tryParse(rewardText);
                        if (rewardValue == null) {
                          ref.read(snackbarProvider).showSnackbar(
                                "La recompensa debe ser un número válido",
                                isError: true,
                              );
                          return;
                        }
                        if (rewardValue < 100 || rewardValue > 1000000) {
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
                            // Clear inputs only on success
                            titleController.clear();
                            descriptionController.clear();
                            rewardController.clear();
                            selectedCategory.value = null;

                            if (context.mounted) {
                              ref.read(snackbarProvider).showSnackbar(
                                    "Favor publicado con éxito",
                                    isError: false,
                                  );
                              Navigator.of(context).pushReplacement(
                                MaterialPageRoute(
                                  builder: (context) =>
                                      const NavigationScreen(initialIndex: 0),
                                ),
                              );
                            }
                          } else {
                            if (context.mounted) {
                              ref.read(snackbarProvider).showSnackbar(
                                    "Error al publicar el favor",
                                    isError: true,
                                  );
                            }
                          }
                        } catch (e) {
                          await AppLogger.logCrash(
                            screen: 'PostFavorScreen',
                            crashInfo: e.toString(),
                          );
                          if (context.mounted) {
                            ref.read(snackbarProvider).showSnackbar(
                                  "Error: $e",
                                  isError: true,
                                );
                          }
                        }
                      },
                style: ElevatedButton.styleFrom(
                  backgroundColor:
                      isUploading ? Colors.grey[300] : AppColors.mikadoYellow,
                  foregroundColor: Colors.black,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                    side: const BorderSide(color: Colors.black, width: 1),
                  ),
                  elevation: 0,
                  shadowColor: Colors.transparent,
                ),
                child: isUploading
                    ? Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const SizedBox(
                            height: 20,
                            width: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: Colors.black,
                            ),
                          ),
                          const SizedBox(width: 10),
                          Text("Publicando...",
                              style: AppTextStyles.oswaldTitle),
                        ],
                      )
                    : Text(
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
  }) =>
      InputDecoration(
        hintText: hintText,
        counterText: counterText,
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(20),
          borderSide: const BorderSide(color: Colors.grey, width: 4),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(20),
          borderSide: const BorderSide(color: Colors.black, width: 2),
        ),
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      );

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

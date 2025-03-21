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
import 'dart:async';
import 'package:senefavores/utils/logger.dart';


class PostFavorScreen extends HookConsumerWidget {
  const PostFavorScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentUser = ref.read(currentUserNotifierProvider);

    // Controllers for user inputs
    final titleController = useTextEditingController();
    final descriptionController = useTextEditingController();
    final rewardController = useTextEditingController();

    // For category selection
    final selectedCategory = useState<String?>(null);

    return SafeArea(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          // Top bar with logo and profile
          const SenefavoresImageAndTitleAndProfile(),

          // "Crear Favor" title to match the mockup
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 8.0),
            child: Text(
              "Crear Favor",
              style: AppTextStyles.oswaldTitle,
            ),
          ),

          // Form Fields
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Title Row
                  Row(
                    children: [
                      Text("Título:", style: AppTextStyles.oswaldSubtitle),
                      const SizedBox(width: 10),
                      Expanded(
                        child: TextField(
                          controller: titleController,
                          decoration: customInputDecoration(),
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
                    maxLines: 4,
                    decoration: customInputDecoration(),
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
                          keyboardType: TextInputType.number,
                          decoration: customInputDecoration(),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 15),

                  // Category Selection
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
                        "Tutoria",
                        AppColors.orangeWeb,
                        selectedCategory,
                      ),
                    ],
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
                onPressed: () async {
                  final start = DateTime.now(); // ⏱️ Start timer

                  if (titleController.text.isEmpty ||
                      descriptionController.text.isEmpty ||
                      rewardController.text.isEmpty ||
                      selectedCategory.value == null) {
                    ref.read(snackbarProvider).showSnackbar(
                        "Por favor llena todos los campos",
                        isError: true);
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
                    final success = await ref
                        .read(uploadFavorStateNotifierProvider.notifier)
                        .uploadFavor(
                      favor: FavorModel(
                        id: '0',
                        title: titleController.text,
                        description: descriptionController.text,
                        category: (selectedCategory.value ?? "favor").toLowerCase(),
                        reward: int.tryParse(rewardController.text) ?? 0,
                        createdAt: DateTime.now(),
                        requestUserId: currentUser!.id,
                      ),
                    );

                    final duration = DateTime.now().difference(start).inMilliseconds; // ⏱️ End
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
                          isError: false);
                    } else {
                      ref.read(snackbarProvider).showSnackbar(
                          "Error al publicar el favor",
                          isError: true);
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

  // A helper to style TextFields
  InputDecoration customInputDecoration() {
    return InputDecoration(
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

  // Widget for category selection buttons
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

import 'package:flutter/material.dart';
// ignore: depend_on_referenced_packages
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/favors/providers/upload_favor_state_notifier_provider.dart.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:senefavores/views/components/senefavores_image_and_title_and_profile.dart';

class PostFavorScreen extends HookConsumerWidget {
  const PostFavorScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentUserAsync = ref.read(currentUserProvider);
    final titleController = useTextEditingController();
    final descriptionController = useTextEditingController();
    final rewardController = useTextEditingController();
    // final estimatedTimeController = useTextEditingController();
    // final startLocationController = useTextEditingController();
    // final endLocationController = useTextEditingController();

    final selectedCategory = useState<String?>(null);

    return Column(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.max,
      children: [
        SenefavoresImageAndTitleAndProfile(),
        Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            mainAxisSize: MainAxisSize.max,
            children: [
              Column(
                mainAxisAlignment: MainAxisAlignment.start,
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.max,
                children: [
                  Row(
                    children: [
                      Text("Título:   ", style: AppTextStyles.oswaldSubtitle),
                      Expanded(
                        child: TextField(
                          controller: titleController,
                          decoration: customInputDecoration(),
                        ),
                      ),
                    ],
                  ),

                  SizedBox(height: 15),

                  // Description Field
                  Text("Descripción:   ", style: AppTextStyles.oswaldSubtitle),
                  SizedBox(height: 5),
                  TextField(
                    controller: descriptionController,
                    maxLines: 4,
                    decoration: customInputDecoration(),
                  ),

                  SizedBox(height: 15),

                  // Reward Field
                  Row(
                    children: [
                      Text("Recompensa:   ",
                          style: AppTextStyles.oswaldSubtitle),
                      Expanded(
                        child: TextField(
                          controller: rewardController,
                          keyboardType: TextInputType.number,
                          decoration: customInputDecoration(),
                        ),
                      ),
                    ],
                  ),

                  SizedBox(height: 15),

                  // Category Selection
                  Text("Categoría:", style: AppTextStyles.oswaldSubtitle),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.start,
                    children: [
                      _categoryButton(
                          "Favor", AppColors.lightRed, selectedCategory),
                      SizedBox(width: 10),
                      _categoryButton(
                          "Compra", Colors.blueAccent, selectedCategory),
                      SizedBox(width: 10),
                      _categoryButton(
                          "Tutoria", Colors.orangeAccent, selectedCategory),
                    ],
                  ),

                  SizedBox(height: 15),
                ],
              ),
            ],
          ),
        ),
        Padding(
          padding: const EdgeInsets.all(20),
          child: SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () async {
                if (titleController.text.isEmpty ||
                    descriptionController.text.isEmpty ||
                    rewardController.text.isEmpty ||
                    selectedCategory.value == null) {
                  ref
                      .read(snackbarProvider)
                      .showSnackbar("Please fill all fields", isError: true);
                } else {
                  currentUserAsync.when(
                    data: (user) async {
                      final success = await ref
                          .read(uploadFavorStateNotifierProvider.notifier)
                          .uploadFavor(
                            favor: FavorModel(
                              id: 0,
                              title: titleController.text,
                              description: descriptionController.text,
                              category: (selectedCategory.value ?? "favor")
                                  .toLowerCase(),
                              reward: int.tryParse(rewardController.text) ?? 0,
                              favorTime: DateTime.now(),
                              createdAt: DateTime.now(),
                              requestUserId: user.id,
                            ),
                          );

                      if (success) {
                        titleController.clear();
                        descriptionController.clear();
                        rewardController.clear();
                        selectedCategory.value = null;
                        ref
                            .read(snackbarProvider)
                            .showSnackbar("Uploaded", isError: false);
                      } else {
                        ref
                            .read(snackbarProvider)
                            .showSnackbar("Upload failed", isError: true);
                      }
                    },
                    loading: () {
                      ref
                          .read(snackbarProvider)
                          .showSnackbar("Loading", isError: false);
                    },
                    error: (error, stack) {
                      ref
                          .read(snackbarProvider)
                          .showSnackbar("Error", isError: true);
                    },
                  );
                }
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.white,
                foregroundColor: Colors.black,
                padding: EdgeInsets.symmetric(vertical: 12),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                  side: BorderSide(color: Colors.black, width: 2),
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

        // Expandable Section - Otros
        // ExpansionTile(
        //   title:
        //       Text("Otros:", style: TextStyle(fontWeight: FontWeight.bold)),
        //   children: [
        //     _textInputField("Tiempo estimado:", estimatedTimeController),
        //     _textInputField("Lugar inicio:", startLocationController),
        //     _textInputField("Lugar final:", endLocationController),
        //   ],
        // ),
      ],
    );
  }

  InputDecoration customInputDecoration() {
    return InputDecoration(
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(20),
        borderSide: BorderSide(
          color: Colors.grey, // ✅ Border color when focused
          width: 4,
        ),
      ),

      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(20),
        borderSide: BorderSide(
          color: Colors.black, // ✅ Border color when not focused
          width: 2,
        ),
      ),
      contentPadding: EdgeInsets.symmetric(
          horizontal: 16, vertical: 12), // ✅ Padding inside field
    );
  }

  // Widget for category selection buttons
  Widget _categoryButton(
      String text, Color color, ValueNotifier<String?> selectedCategory) {
    final isSelected = selectedCategory.value == text;
    return GestureDetector(
      onTap: () => selectedCategory.value = text,
      child: Chip(
        label: Text(text),
        backgroundColor: isSelected ? color.withOpacity(0.8) : Colors.white,
        labelStyle: TextStyle(
          color: isSelected ? Colors.white : Colors.black,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }

  // Widget _textInputField(String label, TextEditingController controller) {
  //   return Padding(
  //     padding: const EdgeInsets.symmetric(vertical: 5),
  //     child: Column(
  //       crossAxisAlignment: CrossAxisAlignment.start,
  //       children: [
  //         Text(label, style: TextStyle(fontWeight: FontWeight.bold)),
  //         TextField(controller: controller),
  //       ],
  //     ),
  //   );
  // }
}

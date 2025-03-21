import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/state/auth/provider/auth_state_notifier_provider.dart';
import 'package:senefavores/state/reviews/providers/user_reviews_provider.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:senefavores/views/components/build_star_rating.dart';
import 'package:senefavores/views/components/senefavores_image_and_title.dart';
import 'package:senefavores/views/profile/components/review_card.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    SystemChrome.setSystemUIOverlayStyle(SystemUiOverlayStyle.dark.copyWith(
      statusBarColor: Colors.white,
    ));

    ref.read(currentUserNotifierProvider.notifier).refreshUser();
    final currentUser = ref.watch(currentUserNotifierProvider);
    ref.refresh(userReviewsProvider(currentUser!.id));
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        automaticallyImplyLeading: false, // Removes the back arrow
        backgroundColor: Colors.white,
        elevation: 0,
        // Use the SenefavoresImageAndTitle widget for the logo/title
        title: const SenefavoresImageAndTitle(),
        actions: [
          IconButton(
            onPressed: () {
              ref.read(authStateProvider.notifier).signOut();
              Navigator.pop(context);
              ref
                  .read(snackbarProvider)
                  .showSnackbar("Logged out successfully");
            },
            icon: const Icon(Icons.logout, color: Colors.black),
          ),
        ],
      ),
      body: Column(
        children: [
          const SizedBox(height: 20),

          // Profile Information Card
          Padding(
            padding: const EdgeInsets.all(12.0),
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.grey.shade300),
                boxShadow: [
                  BoxShadow(
                    color: Colors.grey.withOpacity(0.2),
                    spreadRadius: 2,
                    blurRadius: 1,
                  ),
                ],
              ),
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 32,
                    backgroundColor: Colors.grey.shade300,
                    child:
                        const Icon(Icons.person, size: 40, color: Colors.black),
                  ),
                  const SizedBox(width: 12),

                  // Name, stars, email, phone
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          currentUser!.name ??
                              currentUser.email.split('@').first,
                          style: GoogleFonts.oswald(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        buildStarRating(
                          currentUser.stars ?? 0,
                          color: AppColors.mikadoYellow,
                          size: 18,
                        ),
                        const SizedBox(height: 4),
                        Text(
                          currentUser.email,
                          style: const TextStyle(color: Colors.black54),
                        ),
                        Text(
                          currentUser.phone ?? "No phone number provided",
                          style: const TextStyle(color: Colors.black54),
                        ),
                      ],
                    ),
                  ),

                  // Edit button
                  IconButton(
                    icon: const Icon(Icons.edit),
                    onPressed: () {
                      // Show a dialog to edit phone number
                      showDialog(
                        context: context,
                        builder: (context) {
                          // Default to "0123456789" if user.phone is empty/null
                          final phoneController = TextEditingController(
                            text: (currentUser.phone == null ||
                                    currentUser.phone!.isEmpty)
                                ? "Agregar numero"
                                : currentUser.phone,
                          );
                          return AlertDialog(
                            title: const Text("Editar Número de Teléfono"),
                            content: TextField(
                              controller: phoneController,
                              keyboardType: TextInputType.number,
                              maxLength: 10,
                              decoration: const InputDecoration(
                                labelText: "Ej: 310 123 4567",
                              ),
                              inputFormatters: [
                                FilteringTextInputFormatter.digitsOnly,
                                LengthLimitingTextInputFormatter(10),
                              ],
                            ),
                            actions: [
                              TextButton(
                                onPressed: () async {
                                  final phone = phoneController.text.trim();
                                  // Check length == 10
                                  if (phone.length != 10) {
                                    ref.read(snackbarProvider).showSnackbar(
                                          "El número de teléfono debe tener 10 dígitos",
                                          isError: true,
                                        );
                                    return;
                                  }

                                  // Update phone in Supabase
                                  try {
                                    final supabase = Supabase.instance.client;
                                    final List response = await supabase
                                        .from('clients')
                                        .update({'phone': phone})
                                        .eq('id', currentUser.id)
                                        .select();

                                    if (response.isEmpty) {
                                      ref.read(snackbarProvider).showSnackbar(
                                            "Error: No se pudo actualizar el teléfono",
                                            isError: true,
                                          );
                                    } else {
                                      ref
                                          .read(currentUserNotifierProvider
                                              .notifier)
                                          .updatePhone(phone);

                                      ref.read(snackbarProvider).showSnackbar(
                                            "Número de teléfono actualizado",
                                            isError: false,
                                          );

                                      Navigator.pop(context);
                                    }
                                  } catch (e) {
                                    ref.read(snackbarProvider).showSnackbar(
                                          "Excepción al actualizar: $e",
                                          isError: true,
                                        );
                                  }
                                },
                                style: TextButton.styleFrom(
                                  foregroundColor: Colors.black,
                                ),
                                child: const Text("Guardar"),
                              ),
                              TextButton(
                                onPressed: () => Navigator.pop(context),
                                style: TextButton.styleFrom(
                                  foregroundColor: Colors.black,
                                ),
                                child: const Text("Cancelar"),
                              ),
                            ],
                          );
                        },
                      );
                    },
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 20),

          // Reviews Section Title
          Padding(
            padding: const EdgeInsets.only(left: 16),
            child: Align(
              alignment: Alignment.centerLeft,
              child: Text(
                "Mis reseñas:",
                style: AppTextStyles.oswaldSubtitle,
              ),
            ),
          ),
          const SizedBox(height: 10),

          // Scrollable list of Review Cards
          Expanded(
            child: ref.watch(userReviewsProvider(currentUser.id)).when(
                  data: (reviews) {
                    if (reviews.isEmpty) {
                      return const Center(child: Text("No reviews available."));
                    }
                    return ListView.builder(
                      itemCount: reviews.length,
                      itemBuilder: (context, index) {
                        final review = reviews[index];
                        return ReviewCard(review: review);
                      },
                    );
                  },
                  loading: () => const Center(
                    child: CircularProgressIndicator(color: Colors.black),
                  ),
                  error: (error, stack) => Center(child: Text("Error: $error")),
                ),
          ),
        ],
      ),
    );
  }
}

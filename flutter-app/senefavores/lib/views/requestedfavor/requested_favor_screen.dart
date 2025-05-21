import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/core/format_utils.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/favors/providers/cancel_favor_state_notifier_provider.dart';
import 'package:senefavores/state/favors/providers/complete_favor_state_notifier_provider.dart';
import 'package:senefavores/state/user/providers/user_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:senefavores/views/acceptfavor/components/favor_category_chip.dart';
import 'package:senefavores/views/acceptfavor/components/favor_status_chip.dart';
import 'package:senefavores/views/components/build_star_rating.dart';
import 'package:senefavores/views/components/senefavores_image_and_title_and_profile.dart';
import 'package:senefavores/views/home/components/favor_card_display_at.dart';
import 'package:senefavores/views/requestedfavor/components/senetendero_dialog.dart';
import 'package:senefavores/views/review/upload_review_screen.dart';

class RequestedFavorScreen extends ConsumerWidget {
  final FavorModel favor;
  final FavorScreen favorScreen;

  const RequestedFavorScreen({
    super.key,
    required this.favor,
    required this.favorScreen,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final requesterAsync = ref.watch(userProvider(favor.requestUserId));
    final currentUser = ref.watch(currentUserNotifierProvider);

    return SafeArea(
      child: Scaffold(
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SenefavoresImageAndTitleAndProfile(),
            Padding(
              padding: const EdgeInsets.all(10.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    favor.title,
                    style: AppTextStyles.oswaldTitle.copyWith(
                      fontWeight: FontWeight.w400,
                    ),
                  ),
                  const SizedBox(height: 10),
                  Text(favor.description, style: AppTextStyles.oswaldBody),
                  const SizedBox(height: 10),
                  Text(
                    'Recompensa: ${formatCurrency(favor.reward)}',
                    style: AppTextStyles.oswaldBody,
                  ),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Text('Categoría: ', style: AppTextStyles.oswaldBody),
                      FavorCategoryChip(favor: favor.category),
                    ],
                  ),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Text('Estado: ', style: AppTextStyles.oswaldBody),
                      FavorStatusChip(favor: favor),
                    ],
                  ),
                  const SizedBox(height: 10),
                  Text(
                    'Solicitado por: ',
                    style: AppTextStyles.oswaldSubtitle.copyWith(
                      fontWeight: FontWeight.w300,
                    ),
                  ),
                  const SizedBox(height: 10),
                  requesterAsync.when(
                    data: (reqUser) => Row(
                      children: [
                        const FaIcon(FontAwesomeIcons.circleUser),
                        const SizedBox(width: 10),
                        Text(
                          reqUser.name ?? "Anónimo",
                          style: AppTextStyles.oswaldBody,
                        ),
                        const SizedBox(width: 10),
                        buildStarRating(reqUser.stars ?? 0, size: 18),
                      ],
                    ),
                    loading: () => const CircularProgressIndicator(
                      color: Colors.black,
                    ),
                    error: (error, stack) =>
                        Text('Error: $error', style: AppTextStyles.oswaldBody),
                  ),
                ],
              ),
            ),
            const Spacer(),
            buildFavorActionButtons(
              status: favor.status,
              onDonePressed: () {
                String userToReviewId = favor.requestUserId;
                if (favorScreen == FavorScreen.requested) {
                  userToReviewId = favor.acceptUserId!;
                } else if (favorScreen == FavorScreen.accepted) {
                  userToReviewId = favor.requestUserId!;
                }

                if (favor.status.toLowerCase() != 'done') {
                  ref
                      .watch(completeFavorProvider.notifier)
                      .completeFavor(favorId: favor.id);
                  ref.read(snackbarProvider).showSnackbar(
                      'Favor completado con exito',
                      isError: false);
                }

                Navigator.pop(context);
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => UploadReviewScreen(
                      userToReviewId: userToReviewId,
                      favor: favor,
                      favorScreen: favorScreen,
                    ),
                  ),
                );
              },
              onCancelPressed: () async {
                final notifier = ref.read(cancelFavorProvider.notifier);
                await notifier.cancelFavor(favorId: favor.id);
                ref
                    .read(snackbarProvider)
                    .showSnackbar('Favor cancelado', isError: false);
                Navigator.pop(context);
              },
              onAcceptPressed: () async {
                final favorStatus = favorScreen;

                if (favorStatus == FavorScreen.accepted) {
                  final requesterAsync =
                      ref.watch(userProvider(favor.requestUserId));

                  requesterAsync.whenData((requesterUser) async {
                    if (requesterUser != null) {
                      await showDialog(
                        context: context,
                        builder: (_) => buildCustomSenetenderoDialog(
                            context, requesterUser, favor),
                      );
                    }
                  });
                }

                if (favorStatus == FavorScreen.requested &&
                    favor.acceptUserId != null) {
                  final acceptedAsync =
                      ref.watch(userProvider(favor.acceptUserId!));

                  acceptedAsync.whenData((acceptedUser) async {
                    if (acceptedUser != null) {
                      await showDialog(
                        context: context,
                        builder: (_) => buildCustomSenetenderoDialog(
                            context, acceptedUser, favor),
                      );
                    }
                  });
                }
              },
            ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }
}

Widget buildFavorActionButtons({
  required String status,
  required VoidCallback onDonePressed, // for "Hacer reseña" or "Finalizar"
  required VoidCallback onCancelPressed, // for "Cancelar"
  required VoidCallback onAcceptPressed, // for "Senetendero"
}) {
  final lowerStatus = status.toLowerCase();

  Widget buildButton({
    required String text,
    required Color backgroundColor,
    Color textColor = Colors.white,
    required VoidCallback onPressed,
  }) {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: SizedBox(
        width: double.infinity,
        height: 50,
        child: ElevatedButton(
          onPressed: onPressed,
          style: ElevatedButton.styleFrom(
            backgroundColor: backgroundColor,
            foregroundColor: textColor,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(30),
            ),
            elevation: 0,
            shadowColor: Colors.transparent,
          ),
          child: Text(
            text,
            style: AppTextStyles.oswaldBody.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
      ),
    );
  }

  switch (lowerStatus) {
    case 'done':
      return buildButton(
        text: "Hacer reseña",
        backgroundColor: AppColors.mikadoYellow,
        onPressed: onDonePressed,
      );

    case 'pending':
      return buildButton(
        text: "Cancelar",
        backgroundColor: Colors.red,
        onPressed: onCancelPressed,
      );

    case 'accepted':
      return Column(
        children: [
          buildButton(
            text: "Senetendero",
            backgroundColor: AppColors.lightSkyBlue,
            onPressed: onAcceptPressed,
          ),
          buildButton(
            text: "Finalizar",
            backgroundColor: Colors.amber,
            textColor: Colors.black,
            onPressed: onDonePressed,
          ),
          buildButton(
            text: "Cancelar",
            backgroundColor: Colors.red,
            onPressed: onCancelPressed,
          ),
        ],
      );

    default:
      return const SizedBox.shrink(); // No action for unknown status
  }
}

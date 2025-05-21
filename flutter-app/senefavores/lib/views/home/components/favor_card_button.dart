import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter/material.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/favors/providers/cancel_favor_state_notifier_provider.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:senefavores/state/user/providers/user_provider.dart';
import 'package:senefavores/views/home/components/favor_card_display_at.dart';
import 'package:senefavores/views/requestedfavor/components/senetendero_dialog.dart';
import 'package:senefavores/views/review/upload_review_screen.dart';

class FavorCardButton extends ConsumerWidget {
  final FavorModel favor;
  final FavorScreen favorScreen;

  const FavorCardButton(
      {super.key, required this.favor, required this.favorScreen});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    Color? backgroundColor;
    String? buttonText;

    switch (favor.status.toLowerCase()) {
      case 'done':
        backgroundColor = AppColors.mikadoYellow;
        buttonText = "Hacer reseña";
        break;
      case 'pending':
        backgroundColor = Colors.red;
        buttonText = "Cancelar";
        break;
      case 'accepted':
        backgroundColor = AppColors.lightSkyBlue;
        buttonText = "Senetendero";
        break;
      default:
        return const SizedBox.shrink(); // No button for unknown status
    }

    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: SizedBox(
        width: double.infinity,
        height: 50,
        child: ElevatedButton(
          onPressed: () async {
            if (favor.status.toLowerCase() == 'pending') {
              await ref
                  .read(cancelFavorProvider.notifier)
                  .cancelFavor(favorId: favor.id);
              ref.read(snackbarProvider).showSnackbar(
                    'Favor cancelado',
                  );
            }

            if (favor.status.toLowerCase() == 'accepted') {
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
            }

            if (favor.status.toLowerCase() == 'done') {
              String userToReviewId = favor.requestUserId;
              if (favorScreen == FavorScreen.requested) {
                userToReviewId = favor.acceptUserId!;
              } else if (favorScreen == FavorScreen.accepted) {
                userToReviewId = favor.requestUserId!;
              }

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
            }
          },
          style: ElevatedButton.styleFrom(
            backgroundColor: backgroundColor,
            foregroundColor: Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(30),
            ),
            elevation: 0,
            shadowColor: Colors.transparent,
          ),
          child: Text(
            buttonText,
            style: AppTextStyles.oswaldBody.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
      ),
    );
  }
}

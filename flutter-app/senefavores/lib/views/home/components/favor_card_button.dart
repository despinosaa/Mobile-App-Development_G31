import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:senefavores/core/constant.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/favors/providers/cancel_favor_state_notifier_provider.dart';
import 'package:senefavores/state/reviews/providers/review_exists_provider.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:senefavores/state/user/providers/user_provider.dart';
import 'package:senefavores/views/home/components/favor_card_display_at.dart';
import 'package:senefavores/views/review/upload_review_screen.dart';
import 'package:senefavores/views/requestedfavor/components/senetendero_dialog.dart';

class FavorCardButton extends ConsumerWidget {
  const FavorCardButton({
    super.key,
    required this.favor,
    required this.favorScreen,
  });

  final FavorModel favor;
  final FavorScreen favorScreen; // enum from favor_card_display_at.dart

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    /* ── current user ───────────────────────────────────────────── */
    final currentUser = ref.watch(currentUserNotifierProvider);
    if (currentUser == null) return const SizedBox.shrink();

    /* ── realtime flag: has this user already reviewed? ─────────── */
    final alreadyReviewed = ref.watch(
      reviewExistsProvider(
        (favorId: favor.id, reviewerId: currentUser.id),
      ),
    );

    /* ── decide what to show ────────────────────────────────────── */
    return alreadyReviewed.when(
      loading: () => const _SpinnerPlaceholder(),
      error: (_, __) => const SizedBox.shrink(),
      data: (done) {
        /* 1) If favor is done AND reviewed → show “Reseña enviada ✔” */
        if (favor.status.toLowerCase() == 'done' && done) {
          return const _ReviewSentLabel();
        }

        /* 2) Otherwise decide which action button is valid */
        String? text;
        Color bg = Colors.grey;
        VoidCallback? onPressed;

        switch (favor.status.toLowerCase()) {
          case 'done':
            text = 'Hacer reseña';
            bg = AppColors.mikadoYellow;
            onPressed = () {
              // final guard in case of race
              if (done) {
                ref
                    .read(snackbarProvider)
                    .showSnackbar('Ya enviaste una reseña para este favor');
                return;
              }

              String userToReviewId = favor.requestUserId;
              if (favorScreen == FavorScreen.requested) {
                userToReviewId = favor.acceptUserId!;
              } else if (favorScreen == FavorScreen.accepted) {
                userToReviewId = favor.requestUserId!;
              }

              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => UploadReviewScreen(
                    userToReviewId: userToReviewId,
                    favor: favor,
                    favorScreen: favorScreen,
                  ),
                ),
              );
            };
            break;

          case 'pending':
            text = 'Cancelar';
            bg = Colors.red;
            onPressed = () async {
              await ref
                  .read(cancelFavorProvider.notifier)
                  .cancelFavor(favorId: favor.id);
              ref
                  .read(snackbarProvider)
                  .showSnackbar('Favor cancelado', isError: false);
            };
            break;

          case 'accepted':
            text = 'Senetendero';
            bg = AppColors.lightSkyBlue;
            onPressed = () async {
              // open Senetendero dialog (original logic)
              final targetId = favorScreen == FavorScreen.accepted
                  ? favor.requestUserId
                  : favor.acceptUserId;
              if (targetId == null) return;

              final targetAsync = ref.watch(userProvider(targetId));
              targetAsync.whenData((targetUser) async {
                if (targetUser != null) {
                  await showDialog(
                    context: context,
                    builder: (_) => buildCustomSenetenderoDialog(
                        context, targetUser, favor),
                  );
                }
              });
            };
            break;

          default:
            return const SizedBox.shrink();
        }

        return _ActionButton(
          text: text!,
          background: bg,
          onPressed: onPressed!,
        );
      },
    );
  }
}

/* ── Helper widgets ───────────────────────────────────────────────── */

class _SpinnerPlaceholder extends StatelessWidget {
  const _SpinnerPlaceholder();

  @override
  Widget build(BuildContext context) => const SizedBox(
        width: double.infinity,
        height: 50,
        child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
      );
}

class _ReviewSentLabel extends StatelessWidget {
  const _ReviewSentLabel();

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.all(8.0),
        child: Container(
          width: double.infinity,
          height: 50,
          alignment: Alignment.center,
          decoration: BoxDecoration(
            color: Colors.grey.shade300,
            borderRadius: BorderRadius.circular(30),
          ),
          child: Text(
            'Reseña enviada ✔',
            style: AppTextStyles.oswaldBody,
          ),
        ),
      );
}

class _ActionButton extends StatelessWidget {
  const _ActionButton({
    required this.text,
    required this.background,
    required this.onPressed,
  });

  final String text;
  final Color background;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.all(8.0),
        child: SizedBox(
          width: double.infinity,
          height: 50,
          child: ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: background,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(30),
              ),
              elevation: 0,
              shadowColor: Colors.transparent,
            ),
            onPressed: onPressed,
            child: Text(
              text,
              style: AppTextStyles.oswaldBody
                  .copyWith(fontWeight: FontWeight.bold),
            ),
          ),
        ),
      );
}

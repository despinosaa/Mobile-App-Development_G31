import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/core/format_utils.dart';
import 'package:senefavores/state/connectivity/connectivity_provider.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/favors/providers/cancel_favor_state_notifier_provider.dart';
import 'package:senefavores/state/favors/providers/complete_favor_state_notifier_provider.dart';
import 'package:senefavores/state/reviews/providers/review_exists_provider.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:senefavores/state/user/providers/user_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:senefavores/views/acceptfavor/components/favor_category_chip.dart';
import 'package:senefavores/views/acceptfavor/components/favor_status_chip.dart';
import 'package:senefavores/views/components/build_star_rating.dart';
import 'package:senefavores/views/components/senefavores_image_and_title_and_profile.dart';
import 'package:senefavores/views/home/components/favor_card_display_at.dart';
import 'package:senefavores/views/requestedfavor/components/senetendero_dialog.dart';
import 'package:senefavores/views/review/upload_review_screen.dart';

class RequestedFavorScreen extends ConsumerWidget {
  const RequestedFavorScreen({
    super.key,
    required this.favor,
    required this.favorScreen,
  });

  final FavorModel favor;
  final FavorScreen favorScreen;

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

            /* ---- favor info ---- */
            Padding(
              padding: const EdgeInsets.all(10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(favor.title,
                      style: AppTextStyles.oswaldTitle
                          .copyWith(fontWeight: FontWeight.w400)),
                  const SizedBox(height: 10),
                  Text(favor.description, style: AppTextStyles.oswaldBody),
                  const SizedBox(height: 10),
                  Text('Recompensa: ${formatCurrency(favor.reward)}',
                      style: AppTextStyles.oswaldBody),
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
                  Text('Solicitado por:',
                      style: AppTextStyles.oswaldSubtitle
                          .copyWith(fontWeight: FontWeight.w300)),
                  const SizedBox(height: 10),
                  requesterAsync.when(
                    data: (reqUser) => Row(
                      children: [
                        const FaIcon(FontAwesomeIcons.circleUser),
                        const SizedBox(width: 10),
                        Text(reqUser.name ?? 'Anónimo',
                            style: AppTextStyles.oswaldBody),
                        const SizedBox(width: 10),
                        buildStarRating(reqUser.stars ?? 0, size: 18),
                      ],
                    ),
                    loading: () =>
                        const CircularProgressIndicator(color: Colors.black),
                    error: (e, _) =>
                        Text('Error: $e', style: AppTextStyles.oswaldBody),
                  ),
                ],
              ),
            ),
            const Spacer(),

            /* ---- live action area ---- */
            _ActionArea(
              favor: favor,
              favorScreen: favorScreen,
              currentUserId: currentUser?.id,
            ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }
}

/* ░░░ shared helper widgets ░░░ */
class _Spinner extends StatelessWidget {
  const _Spinner();
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
        padding: const EdgeInsets.all(8),
        child: Container(
          width: double.infinity,
          height: 50,
          alignment: Alignment.center,
          decoration: BoxDecoration(
            color: Colors.grey.shade300,
            borderRadius: BorderRadius.circular(30),
          ),
          child: Text('Reseña enviada ✔', style: AppTextStyles.oswaldBody),
        ),
      );
}

/* ---- action buttons --------------------------------------------------- */
class _ActionArea extends ConsumerWidget {
  const _ActionArea({
    required this.favor,
    required this.favorScreen,
    required this.currentUserId,
  });

  final FavorModel favor;
  final FavorScreen favorScreen;
  final String? currentUserId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final reviewed = currentUserId == null
        ? const AsyncValue<bool>.data(false)
        : ref.watch(
            reviewExistsProvider(
              (favorId: favor.id, reviewerId: currentUserId!),
            ),
          );

    final connectivity = ref.watch(connectivityProvider).value;

    if (reviewed.isLoading) {
      return const Padding(padding: EdgeInsets.all(8), child: _Spinner());
    }

    final bool hasReviewed =
        reviewed.maybeWhen(data: (d) => d, orElse: () => false);

    if (favor.status.toLowerCase() == 'done' && hasReviewed) {
      return const _ReviewSentLabel();
    }

    final bool isOffline = connectivity == ConnectivityResult.none;

    Widget btn(String label, Color bg, VoidCallback onPressed,
            {Color textColor = Colors.white}) =>
        Padding(
          padding: const EdgeInsets.all(8),
          child: SizedBox(
            width: double.infinity,
            height: 50,
            child: ElevatedButton(
              onPressed: onPressed,
              style: ElevatedButton.styleFrom(
                backgroundColor: isOffline ? Colors.grey : bg,
                foregroundColor: textColor,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(30),
                ),
                elevation: 0,
                shadowColor: Colors.transparent,
              ),
              child: Text(
                isOffline ? '$label 📡🚫' : label,
                style: AppTextStyles.oswaldBody
                    .copyWith(fontWeight: FontWeight.bold),
              ),
            ),
          ),
        );

    VoidCallback offlineHandler() => () {
          showDialog(
            context: context,
            builder: (_) => const AlertDialog(
              title: Text('Sin conexión'),
              content: Text('Estas sin internet Oopsie-doopsie!'),
            ),
          );
        };

    switch (favor.status.toLowerCase()) {
      case 'done':
        return btn(
          'Hacer reseña',
          AppColors.mikadoYellow,
          isOffline
              ? offlineHandler()
              : () {
                  if (currentUserId == null) return;
                  String userToReviewId = favor.requestUserId;
                  if (favorScreen == FavorScreen.requested) {
                    userToReviewId = favor.acceptUserId!;
                  } else if (favorScreen == FavorScreen.accepted) {
                    userToReviewId = favor.requestUserId;
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
                },
        );

      case 'pending':
        return btn(
          'Cancelar',
          Colors.red,
          isOffline
              ? offlineHandler()
              : () async {
                  await ref
                      .read(cancelFavorProvider.notifier)
                      .cancelFavor(favorId: favor.id);
                  if (!context.mounted) return;
                  ref
                      .read(snackbarProvider)
                      .showSnackbar('Favor cancelado', isError: false);
                  Navigator.pop(context);
                },
        );

      case 'accepted':
        return Column(
          children: [
            btn(
              'Senetendero',
              AppColors.lightSkyBlue,
              isOffline
                  ? offlineHandler()
                  : () async {
                      final userId = favorScreen == FavorScreen.accepted
                          ? favor.requestUserId
                          : favor.acceptUserId!;
                      try {
                        final targetUser =
                            await ref.read(userProvider(userId).future);
                        if (!context.mounted) return;
                        await showDialog(
                          context: context,
                          builder: (_) => buildCustomSenetenderoDialog(
                            context,
                            targetUser,
                            favor,
                          ),
                        );
                      } catch (error) {
                        if (context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(
                                content: Text('Failed to load user: $error')),
                          );
                        }
                      }
                    },
            ),
            if (favorScreen == FavorScreen.requested)
              btn(
                'Finalizar',
                Colors.amber,
                isOffline
                    ? offlineHandler()
                    : () {
                        ref
                            .read(completeFavorProvider.notifier)
                            .completeFavor(favorId: favor.id);
                        ref.read(snackbarProvider).showSnackbar(
                            'Favor completado con éxito',
                            isError: false);
                        Navigator.pop(context);
                      },
                textColor: Colors.black,
              ),
            btn(
              'Cancelar',
              Colors.red,
              isOffline
                  ? offlineHandler()
                  : () async {
                      await ref
                          .read(cancelFavorProvider.notifier)
                          .cancelFavor(favorId: favor.id);
                      if (!context.mounted) return;
                      ref
                          .read(snackbarProvider)
                          .showSnackbar('Favor cancelado', isError: false);
                      Navigator.pop(context);
                    },
            ),
          ],
        );

      default:
        return Text(favor.status, style: AppTextStyles.oswaldBody);
    }
  }
}

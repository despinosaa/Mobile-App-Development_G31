import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/core/format_utils.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/favors/providers/accept_favor_state_notifier_provider.dart';
import 'package:senefavores/state/user/providers/user_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:senefavores/views/acceptfavor/components/favor_accepted_dialog.dart';
import 'package:senefavores/views/acceptfavor/components/favor_category_chip.dart';
import 'package:senefavores/views/components/build_star_rating.dart';
import 'package:senefavores/views/components/senefavores_image_and_title_and_profile.dart';
import 'package:senefavores/utils/logger.dart';

class AcceptFavorScreen extends ConsumerWidget {
  final FavorModel favor;
  const AcceptFavorScreen({
    super.key,
    required this.favor,
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
                      Text(
                        'Categoría: ',
                        style: AppTextStyles.oswaldBody,
                      ),
                      FavorCategoryChip(favor: favor.category),
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
                    data: (reqUser) {
                      return Row(
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
                      );
                    },
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
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: SizedBox(
                width: double.infinity,
                height: 50,
                child: ElevatedButton(
                  onPressed: () async {
                    final start = DateTime.now();

                    try {
                      final requesterUser = requesterAsync.maybeWhen(
                        data: (reqUser) => reqUser,
                        orElse: () => null,
                      );

                      if (currentUser == null) {
                        ref.read(snackbarProvider).showSnackbar(
                              "Error: No current user found",
                              isError: true,
                            );
                        return;
                      }

                      final bool hasBeenUploaded = await ref
                          .read(acceptFavorProvider.notifier)
                          .acceptFavor(
                            favorId: favor.id,
                            userId: currentUser.id,
                          );

                      final duration =
                          DateTime.now().difference(start).inMilliseconds;
                      await AppLogger.logResponseTime(
                        screen: 'AcceptFavorScreen',
                        responseTimeMs: duration,
                      );

                      if (!hasBeenUploaded) {
                        ref.read(snackbarProvider).showSnackbar(
                              "No se aceptó el favor",
                              isError: true,
                            );
                        return;
                      }

                      if (!context.mounted) return;

                      if (requesterUser == null) {
                        ref.read(snackbarProvider).showSnackbar(
                              "Error: Requester data not loaded",
                              isError: true,
                            );
                        return;
                      }

                      await showDialog(
                        context: context,
                        builder: (context) =>
                            buildCustomDialog(context, requesterUser, favor),
                      );

                      if (!context.mounted) return;
                      Navigator.pop(context);
                    } catch (e) {
                      await AppLogger.logCrash(
                        screen: 'AcceptFavorScreen',
                        crashInfo: e.toString(),
                      );

                      ref.read(snackbarProvider).showSnackbar(
                            "Excepción: $e",
                            isError: true,
                          );
                    }
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.amber,
                    foregroundColor: Colors.black,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(30),
                    ),
                    elevation: 0,
                    shadowColor: Colors.transparent,
                  ),
                  child: Text(
                    "ACEPTAR",
                    style: AppTextStyles.oswaldBody.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }
}

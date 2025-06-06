import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/state/favors/providers/mis_favores_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:senefavores/state/user/providers/user_provider.dart';
import 'package:senefavores/views/components/senefavores_image_and_title_and_profile.dart';
import 'package:senefavores/views/home/components/favor_card.dart';
import 'package:senefavores/utils/logger.dart';
import 'package:senefavores/views/home/components/favor_card_display_at.dart';
import 'package:senefavores/views/requestedfavor/requested_favor_screen.dart';

class MisFavoresView extends ConsumerStatefulWidget {
  const MisFavoresView({super.key});

  @override
  ConsumerState<MisFavoresView> createState() => _MisFavoresViewState();
}

class _MisFavoresViewState extends ConsumerState<MisFavoresView> {
  bool showSolicitados = false;

  @override
  Widget build(BuildContext context) {
    final currentUser = ref.watch(currentUserNotifierProvider);

    return SafeArea(
      child: Column(
        children: [
          const SenefavoresImageAndTitleAndProfile(),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Row(
              children: [
                Expanded(
                  child: GestureDetector(
                    onTap: () => setState(() => showSolicitados = false),
                    child: Container(
                      height: 40,
                      alignment: Alignment.center,
                      decoration: BoxDecoration(
                        color: !showSolicitados
                            ? AppColors.mikadoYellow
                            : Colors.white,
                        border: Border.all(color: Colors.black),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        "Aceptados",
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          color: !showSolicitados
                              ? Colors.black
                              : Colors.grey[700],
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: GestureDetector(
                    onTap: () => setState(() => showSolicitados = true),
                    child: Container(
                      height: 40,
                      alignment: Alignment.center,
                      decoration: BoxDecoration(
                        color: showSolicitados
                            ? AppColors.mikadoYellow
                            : Colors.white,
                        border: Border.all(color: Colors.black),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        "Solicitados",
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          color:
                              showSolicitados ? Colors.black : Colors.grey[700],
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
          Expanded(
            child: showSolicitados
                ? _buildSolicitadosList(currentUser!.id)
                : _buildAceptadosList(currentUser!.id),
          ),
        ],
      ),
    );
  }

  Widget _buildSolicitadosList(String userId) {
    final favorsRequested = ref.watch(favorsRequestedByUserProvider(userId));

    return favorsRequested.when(
      loading: () => const Center(
          child: CircularProgressIndicator(
        color: Colors.black,
      )),
      error: (error, stack) {
        AppLogger.logCrash(
          screen: 'MisFavoresView',
          crashInfo: error.toString(),
        );
        //return Center(child: Text("Error: $error"));
        return const SizedBox(
          height: 0,
        );
      },
      data: (favorsList) {
        if (favorsList.isEmpty) {
          return const Center(child: Text("No has solicitado favores"));
        }
        return ListView.builder(
          itemCount: favorsList.length,
          itemBuilder: (context, index) {
            final favor = favorsList[index];
            final userAsync = ref.watch(userProvider(favor.requestUserId));

            return userAsync.when(
              loading: () => const Padding(
                padding: EdgeInsets.symmetric(vertical: 10),
                child: Center(
                    child: CircularProgressIndicator(
                  color: Colors.black,
                )),
              ),
              error: (error, stack) {
                /*return Padding(
                  padding: const EdgeInsets.symmetric(vertical: 10),
                  child: Center(child: Text("Error loading user: $error")),
                );*/
                return const SizedBox(
                  height: 0,
                );
              },
              data: (reqUser) => InkWell(
                child: FavorCard(
                  favor: favor,
                  user: reqUser,
                  showButton: true,
                  favorScreen: FavorScreen.requested,
                ),
                onTap: () => Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => RequestedFavorScreen(
                      favor: favor,
                      favorScreen: FavorScreen.requested,
                    ),
                  ),
                ),
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildAceptadosList(String userId) {
    final favorsAccepted = ref.watch(favorsAcceptedByUserProvider(userId));

    return favorsAccepted.when(
      loading: () => const Center(
        child: CircularProgressIndicator(
          color: Colors.black,
        ),
      ),
      error: (error, stack) {
        AppLogger.logCrash(
          screen: 'MisFavoresView',
          crashInfo: error.toString(),
        );
        //return Center(child: Text("Error: $error"));
        return const SizedBox(
          height: 0,
        );
      },
      data: (favorsList) {
        if (favorsList.isEmpty) {
          return const Center(child: Text("No has aceptado favores"));
        }
        return ListView.builder(
          itemCount: favorsList.length,
          itemBuilder: (context, index) {
            final favor = favorsList[index];
            final userAsync = ref.watch(userProvider(favor.requestUserId));

            return userAsync.when(
              loading: () => const Padding(
                padding: EdgeInsets.symmetric(vertical: 10),
                child: Center(child: CircularProgressIndicator()),
              ),
              error: (error, stack) {
                /*return Padding(
                  padding: const EdgeInsets.symmetric(vertical: 10),
                  child: Center(child: Text("Error loading user: $error")),
                );*/
                return const SizedBox(
                  height: 0,
                );
              },
              data: (reqUser) => InkWell(
                child: FavorCard(
                  favor: favor,
                  user: reqUser,
                  showButton: true,
                  favorScreen: FavorScreen.accepted,
                ),
                onTap: () => Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => RequestedFavorScreen(
                      favor: favor,
                      favorScreen: FavorScreen.accepted,
                    ),
                  ),
                ),
              ),
            );
          },
        );
      },
    );
  }
}

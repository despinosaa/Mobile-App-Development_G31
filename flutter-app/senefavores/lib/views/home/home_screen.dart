import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/favors/providers/favors_provider.dart';
import 'package:senefavores/state/home/models/filter_button_category.dart';
import 'package:senefavores/state/home/models/filter_button_sort.dart';
import 'package:senefavores/state/home/models/smart_sorting.dart';
import 'package:senefavores/state/home/providers/selected_category_filter_button_provider.dart';
import 'package:senefavores/state/home/providers/selected_sort_filter_button_provider.dart';
import 'package:senefavores/state/home/providers/smart_sorting_state_notifier_provider.dart';
import 'package:senefavores/state/user/providers/user_provider.dart';
import 'package:senefavores/views/acceptfavor/accept_favor_screen.dart';
import 'package:senefavores/views/components/senefavores_image_and_title_and_profile.dart';
import 'package:senefavores/views/home/components/category_filter_button.dart';
import 'package:senefavores/views/home/components/favor_card.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/utils/logger.dart';

class HomeScreen extends ConsumerStatefulWidget {
  const HomeScreen({super.key});

  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen> {
  @override
  Widget build(BuildContext context) {
    final favors = ref.watch(favorsStreamProvider);
    final filter = ref.watch(selectedSortFilterButtonProvider);
    final smartSorting = ref.watch(smartSortingStateNotifierProvider);

    return SafeArea(
      child: Column(
        children: [
          SenefavoresImageAndTitleAndProfile(),
          Row(
            mainAxisSize: MainAxisSize.max,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  IconButton(
                    onPressed: () {
                      if (smartSorting == SmartSorting.disabled) {
                        ref
                            .read(selectedSortFilterButtonProvider.notifier)
                            .set(FilterButtonSort.asc);
                        ref
                            .read(selectedCategoryFilterButtonProvider.notifier)
                            .setCategoryFilter(FilterButtonCategory.none);
                      }

                      ref
                          .read(smartSortingStateNotifierProvider.notifier)
                          .toggleSorting();
                    },
                    icon: FaIcon(
                      FontAwesomeIcons.magicWandSparkles,
                      color: smartSorting == SmartSorting.enabled
                          ? Colors.green
                          : Colors.black,
                    ),
                  ),
                  IconButton(
                    onPressed: () {
                      ref
                          .read(smartSortingStateNotifierProvider.notifier)
                          .setSorting(SmartSorting.disabled);

                      ref
                          .read(selectedSortFilterButtonProvider.notifier)
                          .toggle();
                    },
                    icon: filter == FilterButtonSort.desc
                        ? FaIcon(FontAwesomeIcons.arrowDownWideShort)
                        : FaIcon(FontAwesomeIcons.arrowUpWideShort),
                  ),
                ],
              ),
              Row(
                mainAxisSize: MainAxisSize.max,
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      CategoryFilterButton(
                        backgroundColor: AppColors.lightRed,
                        text: "Favor",
                        isSelected: false,
                        filterButtonCategory: FilterButtonCategory.favor,
                      ),
                      CategoryFilterButton(
                        backgroundColor: AppColors.lightSkyBlue,
                        text: "Compra",
                        isSelected: false,
                        filterButtonCategory: FilterButtonCategory.compra,
                      ),
                      CategoryFilterButton(
                        backgroundColor: AppColors.orangeWeb,
                        text: "Tutoría",
                        isSelected: false,
                        filterButtonCategory: FilterButtonCategory.tutoria,
                      ),
                    ],
                  )
                ],
              ),
            ],
          ),
          Expanded(
            child: favors.when(
              data: (favorsList) {
                if (favorsList.isEmpty) {
                  return Center(child: Text("No hay favores disponibles"));
                }

                return ListView.builder(
                  shrinkWrap: true,
                  physics: const BouncingScrollPhysics(),
                  itemCount: favorsList.length,
                  itemBuilder: (context, index) {
                    final favor = favorsList[index];
                    final userAsync =
                        ref.watch(userProvider(favor.requestUserId));

                    return userAsync.when(
                      data: (user) => InkWell(
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => AcceptFavorScreen(
                                favor: favor,
                              ),
                            ),
                          );
                        },
                        child: FavorCard(favor: favor, user: user),
                      ),
                      loading: () => const Padding(
                        padding: EdgeInsets.symmetric(vertical: 10),
                        child: Center(child: CircularProgressIndicator()),
                      ),
                      error: (error, stack) {
                        return Padding(
                          padding: const EdgeInsets.symmetric(vertical: 10),
                          child:
                              Center(child: Text("Error loading user: $error")),
                        );
                      },
                    );
                  },
                );
              },
              loading: () => const Center(
                  child: CircularProgressIndicator(
                color: Colors.black,
              )),
              error: (error, stack) {
                AppLogger.logCrash(
                  screen: 'HomeScreen',
                  crashInfo: error.toString(),
                );

                if (error.toString() ==
                    'Null check operator used on a null value') {
                  return const Center(
                      child: CircularProgressIndicator(
                    color: Colors.black,
                  ));
                }
                return Center(child: Text("Error loading favors: $error"));
              },
            ),
          ),
        ],
      ),
    );
  }
}

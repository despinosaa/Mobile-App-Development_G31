import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/favors/providers/favors_provider.dart';
import 'package:senefavores/state/home/models/filter_button_category.dart';
import 'package:senefavores/state/home/models/filter_button_sort.dart';
import 'package:senefavores/state/home/providers/selected_sort_filter_button_provider.dart';
import 'package:senefavores/state/user/providers/user_provider.dart';
import 'package:senefavores/views/components/senefavores_image_and_title.dart';
import 'package:senefavores/views/home/components/category_filter_button.dart';
import 'package:senefavores/views/home/components/favor_card.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/views/profile/profile_screen.dart';

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

    return SafeArea(
      child: Column(
        children: [
          Row(
            mainAxisSize: MainAxisSize.max,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              SenefavoresImageAndTitle(),
              IconButton(
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => ProfileScreen()),
                  );
                },
                icon: FaIcon(FontAwesomeIcons.circleUser),
              ),
              // Filter row
            ],
          ),
          Row(
            mainAxisSize: MainAxisSize.max,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  IconButton(
                      onPressed: () {}, icon: FaIcon(FontAwesomeIcons.filter)),
                  IconButton(
                    onPressed: () {
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
                        text: "Tutoria",
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
                      data: (user) => FavorCard(favor: favor, user: user),
                      loading: () => const Padding(
                        padding: EdgeInsets.symmetric(vertical: 10),
                        child: Center(child: CircularProgressIndicator()),
                      ),
                      error: (error, stack) => Padding(
                        padding: const EdgeInsets.symmetric(vertical: 10),
                        child:
                            Center(child: Text("Error loading user: $error")),
                      ),
                    );
                  },
                );
              },
              loading: () => const Center(
                  child: CircularProgressIndicator(
                color: Colors.black,
              )),
              error: (error, stack) => Center(child: Text("Error: $error")),
            ),
          ),
        ],
      ),
    );
  }
}

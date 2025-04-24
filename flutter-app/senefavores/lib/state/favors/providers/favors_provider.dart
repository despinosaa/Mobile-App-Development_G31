import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/core/extension.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/home/models/filter_button_category.dart';
import 'package:senefavores/state/home/models/filter_button_sort.dart';
import 'package:senefavores/state/home/models/smart_sorting.dart';
import 'package:senefavores/state/home/providers/selected_category_filter_button_provider.dart';
import 'package:senefavores/state/home/providers/selected_sort_filter_button_provider.dart';
import 'package:senefavores/state/home/providers/smart_sorting_state_notifier_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:senefavores/utils/logger.dart';

final favorsStreamProvider =
    StreamProvider.autoDispose<List<FavorModel>>((ref) async* {
  final supabase = Supabase.instance.client;

  final selectedCategory = ref.watch(selectedCategoryFilterButtonProvider);
  final selectedSort = ref.watch(selectedSortFilterButtonProvider);
  final currentUser = ref.watch(currentUserNotifierProvider);
  final smartSorting = ref.watch(smartSortingStateNotifierProvider);

  if (currentUser == null) {
    yield [];
    return;
  }

  Stream<List<FavorModel>> favorStream;

  if (smartSorting == SmartSorting.enabled) {
    final acceptedFavors = await supabase
        .from('favors')
        .select('category')
        .eq('accept_user_id', currentUser.id);

    final categoryCount = <String, int>{};
    for (var favor in acceptedFavors) {
      String category = favor['category'];
      categoryCount[category] = (categoryCount[category] ?? 0) + 1;
    }

    final sortedCategories = categoryCount.entries.toList()
      ..sort((a, b) => b.value.compareTo(a.value));
    final preferredCategories = sortedCategories.map((e) => e.key).toList();

    final start = DateTime.now();

    favorStream = supabase
        .from('favors')
        .stream(primaryKey: ['id'])
        .neq('request_user_id', currentUser.id)
        .order('created_at', ascending: selectedSort == FilterButtonSort.asc)
        .map((data) {
          final duration = DateTime.now().difference(start).inMilliseconds;
          AppLogger.logResponseTime(
            screen: 'HomeScreen',
            responseTimeMs: duration,
          );
          return data
              .map((favor) => FavorModel.fromJson(favor))
              .where((favor) => favor.acceptUserId == null)
              .toList();
        });

    favorStream = favorStream.map((favors) {
      favors.sort((a, b) {
        int aPriority = preferredCategories.contains(a.category)
            ? preferredCategories.indexOf(a.category)
            : preferredCategories.length;
        int bPriority = preferredCategories.contains(b.category)
            ? preferredCategories.indexOf(b.category)
            : preferredCategories.length;
        return aPriority.compareTo(bPriority);
      });
      return favors;
    });

    yield* favorStream;
    return;
  }

  // If Smart Sorting is disabled, fetch favors normally
  if (selectedCategory == FilterButtonCategory.none) {
    final start = DateTime.now();

    yield* supabase
        .from('favors')
        .stream(primaryKey: ['id'])
        .neq('request_user_id', currentUser.id)
        .order('created_at', ascending: selectedSort == FilterButtonSort.asc)
        .map((data) {
          final duration = DateTime.now().difference(start).inMilliseconds;
          AppLogger.logResponseTime(
            screen: 'HomeScreen',
            responseTimeMs: duration,
          );
          return data
              .map((favor) => FavorModel.fromJson(favor))
              .where((favor) => favor.acceptUserId == null)
              .toList();
        });
  } else {
    final start = DateTime.now();

    String category = selectedCategory.toString().split('.').last.capitalize();

    if (category == "Tutoria") {
      category = "Tutoría";
    }

    yield* supabase
        .from('favors')
        .stream(primaryKey: ['id'])
        .eq('category', category)
        .order('created_at', ascending: selectedSort == FilterButtonSort.asc)
        .map((data) {
          final duration = DateTime.now().difference(start).inMilliseconds;
          AppLogger.logResponseTime(
            screen: 'HomeScreen',
            responseTimeMs: duration,
          );
          return data
              .map((favor) => FavorModel.fromJson(favor))
              .where((favor) =>
                  favor.requestUserId != currentUser.id &&
                  favor.acceptUserId == null)
              .toList();
        });
  }
});

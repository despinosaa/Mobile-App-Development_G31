import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/home/models/filter_button_category.dart';
import 'package:senefavores/state/home/models/filter_button_sort.dart';
import 'package:senefavores/state/home/providers/selected_category_filter_button_provider.dart';
import 'package:senefavores/state/home/providers/selected_sort_filter_button_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

final favorsStreamProvider =
    StreamProvider.autoDispose<List<FavorModel>>((ref) {
  final supabase = Supabase.instance.client;

  final selectedCategory = ref.watch(selectedCategoryFilterButtonProvider);
  final selectedSort = ref.watch(selectedSortFilterButtonProvider);
  final currentUser = ref.watch(currentUserNotifierProvider);

  if (selectedCategory == FilterButtonCategory.none) {
    return supabase
        .from('favors')
        .stream(primaryKey: ['id'])
        .neq('request_user_id', currentUser!.id)
        .order('created_at', ascending: selectedSort == FilterButtonSort.asc)
        .map((data) => data
            .map((favor) => FavorModel.fromJson(favor))
            .where((favor) => favor.acceptUserId == null)
            .toList());
  } else {
    return supabase
        .from('favors')
        .stream(primaryKey: ['id'])
        .eq('category', selectedCategory.toString().split('.').last)
        .order('created_at', ascending: selectedSort == FilterButtonSort.asc)
        .map((data) => data
            .map((favor) => FavorModel.fromJson(favor))
            .where((favor) =>
                favor.requestUserId != currentUser!.id &&
                favor.acceptUserId == null)
            .toList());
  }
});

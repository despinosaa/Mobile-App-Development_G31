import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/home/models/filter_button_category.dart';

class SelectedCategoryFilterButtonNotifier
    extends StateNotifier<FilterButtonCategory> {
  SelectedCategoryFilterButtonNotifier() : super(FilterButtonCategory.none);

  void setCategoryFilter(FilterButtonCategory categoryFilter) {
    state = categoryFilter;
  }
}

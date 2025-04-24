import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/home/models/filter_button_sort.dart';

class SelectedSortFilterButtonNotifier extends StateNotifier<FilterButtonSort> {
  SelectedSortFilterButtonNotifier() : super(FilterButtonSort.desc);

  void toggle() {
    state = state == FilterButtonSort.asc
        ? FilterButtonSort.desc
        : FilterButtonSort.asc;
  }

  void set(FilterButtonSort filter) {
    state = filter;
  }
}

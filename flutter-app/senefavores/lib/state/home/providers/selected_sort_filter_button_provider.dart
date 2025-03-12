import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/home/models/filter_button_sort.dart';
import 'package:senefavores/state/home/notifiers/selected_sort_filter_button_notifier.dart';

final selectedSortFilterButtonProvider =
    StateNotifierProvider<SelectedSortFilterButtonNotifier, FilterButtonSort>(
  (ref) => SelectedSortFilterButtonNotifier(),
);

import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/home/models/filter_button_category.dart';
import 'package:senefavores/state/home/notifiers/selected_category_filter_button_notifier.dart';

final selectedCategoryFilterButtonProvider = StateNotifierProvider<
    SelectedCategoryFilterButtonNotifier, FilterButtonCategory>(
  (ref) => SelectedCategoryFilterButtonNotifier(),
);

import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/home/models/smart_sorting.dart';
import 'package:senefavores/state/home/notifiers/smart_sorting_state_notifer.dart';

final smartSortingStateNotifierProvider =
    StateNotifierProvider<SmartSortingStateNotifier, SmartSorting>(
  (ref) => SmartSortingStateNotifier(),
);

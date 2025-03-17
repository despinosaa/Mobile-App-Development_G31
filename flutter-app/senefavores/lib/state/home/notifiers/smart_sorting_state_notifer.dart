import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/home/models/smart_sorting.dart';

class SmartSortingStateNotifier extends StateNotifier<SmartSorting> {
  SmartSortingStateNotifier() : super(SmartSorting.disabled);

  void toggleSorting() {
    state = (state == SmartSorting.enabled)
        ? SmartSorting.disabled
        : SmartSorting.enabled;
  }

  void setSorting(SmartSorting sorting) {
    state = sorting;
  }
}

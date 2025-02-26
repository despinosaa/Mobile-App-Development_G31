import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/home/models/filter_button_category.dart';
import 'package:senefavores/state/home/providers/selected_category_filter_button_provider.dart';

class CategoryFilterButton extends ConsumerWidget {
  final Color textColor;
  final Color backgroundColor;
  final String text;
  final bool isSelected;
  final FilterButtonCategory filterButtonCategory;

  const CategoryFilterButton({
    super.key,
    this.textColor = Colors.black,
    required this.backgroundColor,
    required this.text,
    required this.isSelected,
    required this.filterButtonCategory,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    FilterButtonCategory selectedFilterButtonCategory =
        ref.watch(selectedCategoryFilterButtonProvider);

    return InkWell(
      onTap: () {
        if (selectedFilterButtonCategory == filterButtonCategory) {
          ref
              .read(selectedCategoryFilterButtonProvider.notifier)
              .setCategoryFilter(FilterButtonCategory.none);
        } else {
          ref
              .read(selectedCategoryFilterButtonProvider.notifier)
              .setCategoryFilter(filterButtonCategory);
        }
      },
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4.0),
        child: Container(
          alignment: Alignment.center,
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
          decoration: BoxDecoration(
            color: (filterButtonCategory == selectedFilterButtonCategory ||
                    selectedFilterButtonCategory == FilterButtonCategory.none)
                ? backgroundColor
                : backgroundColor.withOpacity(0.4),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Text(text),
        ),
      ),
    );
  }
}

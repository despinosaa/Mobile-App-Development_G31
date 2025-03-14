import 'package:flutter/material.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/core/extension.dart';
import 'package:senefavores/state/home/models/filter_button_category.dart';

class FavorCategoryChip extends StatelessWidget {
  final String favor;
  const FavorCategoryChip({super.key, required this.favor});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 20, vertical: 7),
      decoration: BoxDecoration(
        color: favor == FilterButtonCategory.favor.name
            ? AppColors.lightRed
            : favor == FilterButtonCategory.compra.name
                ? AppColors.lightSkyBlue
                : AppColors.orangeWeb,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        favor.capitalize(),
        style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold),
      ),
    );
  }
}

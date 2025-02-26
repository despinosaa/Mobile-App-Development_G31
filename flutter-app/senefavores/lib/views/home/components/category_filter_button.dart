import 'package:flutter/material.dart';

class CategoryFilterButton extends StatelessWidget {
  final Color textColor;
  final Color backgroundColor;
  final String text;
  final bool isSelected;

  const CategoryFilterButton({
    super.key,
    this.textColor = Colors.black,
    required this.backgroundColor,
    required this.text,
    required this.isSelected,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 4.0),
      child: Container(
        alignment: Alignment.center,
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
        decoration: BoxDecoration(
          color: backgroundColor,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Text(text),
      ),
    );
  }
}

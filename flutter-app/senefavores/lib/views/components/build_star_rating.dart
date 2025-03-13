import 'package:flutter/material.dart';

Widget buildStarRating(double stars,
    {Color color = Colors.black, double size = 24}) {
  return Row(
    children: List.generate(5, (index) {
      double starPosition = index + 1;

      if (stars >= starPosition) {
        return Icon(
          Icons.star,
          color: color,
          size: size,
        );
      } else if (stars >= starPosition - 0.5) {
        return Icon(
          Icons.star_half,
          color: color,
          size: size,
        );
      } else {
        return Icon(
          Icons.star_border,
          color: color,
          size: size,
        );
      }
    }),
  );
}

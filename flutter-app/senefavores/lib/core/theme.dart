import 'package:flutter/material.dart';

class AppTheme {
  static final ThemeData lightTheme = ThemeData(
    colorScheme: ColorScheme.fromSeed(seedColor: Colors.yellowAccent),
    useMaterial3: true,
    textTheme: const TextTheme(
      titleLarge: TextStyle(fontSize: 30, fontWeight: FontWeight.bold),
      bodyLarge: TextStyle(fontSize: 18),
    ),
  );
}

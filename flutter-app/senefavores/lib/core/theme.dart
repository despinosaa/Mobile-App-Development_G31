import 'package:flutter/material.dart';

class AppTheme {
  static final ThemeData lightTheme = ThemeData(
    colorScheme: const ColorScheme.light(
      primary: Colors.white, // Primary color as white
      onPrimary: Colors.black, // Text color on white background
    ),
    scaffoldBackgroundColor: Colors.white, // Scaffold background white
    appBarTheme: const AppBarTheme(
      backgroundColor: Colors.white, // AppBar white
      iconTheme: IconThemeData(color: Colors.black), // Icons black
      titleTextStyle: TextStyle(color: Colors.black, fontSize: 20),
      elevation: 0, // Optional: remove AppBar shadow
    ),
    textTheme: const TextTheme(
      bodyMedium: TextStyle(color: Colors.black),
    ),
  );
}

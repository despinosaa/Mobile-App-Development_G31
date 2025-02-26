import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/views/navigation/navigation_screen.dart';

void main() {
  runApp(
    const ProviderScope(
      child: MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter Demo',
      theme: ThemeData(
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
      ),
      home: SafeArea(child: NavigationScreen()),
    );
  }
}

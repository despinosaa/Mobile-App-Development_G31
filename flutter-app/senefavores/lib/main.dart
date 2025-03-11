import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/views/navigation/navigation_screen.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Supabase.initialize(
    url: 'https://kebumzcxttyquorhiicf.supabase.co',
    anonKey:
        'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtlYnVtemN4dHR5cXVvcmhpaWNmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDE2NDM1MDQsImV4cCI6MjA1NzIxOTUwNH0.PiAnATAnWk_7Brz6XzZqQMkaCoGOItFGKhy1EZ8OnVg',
  );
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

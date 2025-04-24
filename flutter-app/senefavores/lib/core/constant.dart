import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class AppColors {
  static const Color lightGray = Color(0xFFF4F4F4);
  static const Color lightMediumGray = Color(0xFFD8D8D8);
  static const Color silver = Color(0xFFC9C9C9);
  static const Color mediumDarkGray = Color(0xFF646464);
  static const Color darkGray = Color(0xFF323232);
  // Custom Colors
  static const Color mikadoYellow = Color(0xFFF6A60A);
  static const Color orangeWeb = Color(0xFFFBB14B);
  static const Color lightRed = Color(0xFFFB9090);
  static const Color lightSkyBlue = Color(0xFF8DC4EC);
}

class AppTextStyles {
  // Titles (Bold)
  static final TextStyle oswaldTitle = GoogleFonts.oswald(
    fontSize: 32,
    fontWeight: FontWeight.bold,
    color: AppColors.darkGray,
  );

  // Subtitles (Semi-Bold)
  static final TextStyle oswaldSubtitle = GoogleFonts.oswald(
    fontSize: 18,
    fontWeight: FontWeight.bold,
    color: Colors.black,
  );

  // Body Text (Regular)
  static final TextStyle oswaldBody = GoogleFonts.oswald(
    fontSize: 16,
    fontWeight: FontWeight.w400,
    color: Colors.black,
  );

  // Buttons (Medium Weight)
  static final TextStyle oswaldButton = GoogleFonts.oswald(
    fontSize: 14,
    fontWeight: FontWeight.w600,
    color: Colors.black,
  );

  // Small Labels
  static final TextStyle oswaldSmall = GoogleFonts.oswald(
    fontSize: 14,
    fontWeight: FontWeight.w300,
    color: Colors.black,
  );
}

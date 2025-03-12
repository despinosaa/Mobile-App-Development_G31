import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class SenefavoresImageAndTitle extends StatelessWidget {
  const SenefavoresImageAndTitle({super.key});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Image.asset(
          'assets/images/senefavores_logo.png',
          height: 50,
        ),
        Text(
          "SeneFavores",
          style: GoogleFonts.oswald(fontSize: 24),
        ),
      ],
    );
  }
}

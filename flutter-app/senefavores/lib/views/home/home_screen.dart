import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:senefavores/views/home/components/category_filter_button.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          mainAxisSize: MainAxisSize.max,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Image.asset(
                  'assets/images/senefavores_logo.png',
                  height: 50,
                ),
                Text(
                  "Senefavores",
                  style: GoogleFonts.oswald(
                    fontSize: 24,
                  ),
                ),
              ],
            ),
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  onPressed: () {},
                  icon: Icon(
                    Icons.person_2_outlined,
                    size: 35,
                    color: Colors.black,
                  ),
                ),
              ],
            ),
          ],
        ),
        Row(
          mainAxisSize: MainAxisSize.max,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              children: [
                IconButton(onPressed: () {}, icon: Icon(Icons.search)),
                IconButton(onPressed: () {}, icon: Icon(Icons.filter_alt))
              ],
            ),
            Row(
              children: [
                CategoryFilterButton(
                  backgroundColor: Colors.red,
                  text: "Favor",
                  isSelected: false,
                ),
                CategoryFilterButton(
                  backgroundColor: Colors.blue,
                  text: "Compra",
                  isSelected: false,
                ),
                CategoryFilterButton(
                  backgroundColor: Colors.yellow,
                  text: "Tutoria",
                  isSelected: false,
                ),
              ],
            )
          ],
        ),
      ],
    );
  }
}

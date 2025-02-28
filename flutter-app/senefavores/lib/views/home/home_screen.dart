import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:senefavores/state/home/models/filter_button_category.dart';
import 'package:senefavores/views/home/components/category_filter_button.dart';
import 'package:senefavores/views/home/components/favor_card.dart';

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
                  icon: FaIcon(FontAwesomeIcons.circleUser),
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
                IconButton(
                    onPressed: () {}, icon: FaIcon(FontAwesomeIcons.filter)),
                IconButton(
                    onPressed: () {},
                    icon: FaIcon(FontAwesomeIcons.arrowDownWideShort)),
              ],
            ),
            Row(
              children: [
                CategoryFilterButton(
                  backgroundColor: Colors.red,
                  text: "Favor",
                  isSelected: false,
                  filterButtonCategory: FilterButtonCategory.favor,
                ),
                CategoryFilterButton(
                  backgroundColor: Colors.blue,
                  text: "Compra",
                  isSelected: false,
                  filterButtonCategory: FilterButtonCategory.compra,
                ),
                CategoryFilterButton(
                  backgroundColor: Colors.yellow,
                  text: "Tutoria",
                  isSelected: false,
                  filterButtonCategory: FilterButtonCategory.tutoria,
                ),
              ],
            )
          ],
        ),
        Expanded(
          child: ListView(
            shrinkWrap: true,
            children: [
              FavorCard(),
              FavorCard(),
              FavorCard(),
              FavorCard(),
            ],
          ),
        ),
      ],
    );
  }
}

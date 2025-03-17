import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:senefavores/views/home/home_screen.dart';
import 'package:senefavores/views/misfavores/mis_favores_view.dart';
import 'package:senefavores/views/postfavor/post_favor_screen.dart';

class NavigationScreen extends StatefulWidget {
  final int initialIndex;
  const NavigationScreen({super.key, this.initialIndex = 0});

  @override
  State<NavigationScreen> createState() => _NavigationScreenState();
}

class _NavigationScreenState extends State<NavigationScreen> {
  late int _selectedIndex;
  static const List<Widget> _widgetOptions = <Widget>[
    HomeScreen(),
    PostFavorScreen(),
    MisFavoresView(),
  ];

  @override
  void initState() {
    super.initState();
    _selectedIndex = widget.initialIndex;
  }

  void _onItemTapped(int index) {
    if (index != _selectedIndex) {
      setState(() {
        _selectedIndex = index;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(child: _widgetOptions.elementAt(_selectedIndex)),
      bottomNavigationBar: BottomNavigationBar(
        items: const [
          BottomNavigationBarItem(
            icon: FaIcon(FontAwesomeIcons.igloo),
            label: 'Home',
          ),
          BottomNavigationBarItem(
            icon: FaIcon(FontAwesomeIcons.circlePlus),
            label: 'Pedir favor',
          ),
          BottomNavigationBarItem(
            icon: FaIcon(FontAwesomeIcons.clockRotateLeft),
            label: 'Mis favores',
          ),
        ],
        type: BottomNavigationBarType.fixed,
        elevation: 0,
        currentIndex: _selectedIndex,
        selectedItemColor: Colors.black,
        onTap: _onItemTapped,
        selectedFontSize: 12,
      ),
    );
  }
}

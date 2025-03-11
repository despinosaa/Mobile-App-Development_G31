import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/auth/provider/auth_state_notifier_provider.dart';
import 'package:senefavores/views/home/home_screen.dart';
import 'package:senefavores/views/misfavores/mis_favores_view.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class NavigationScreen extends StatefulWidget {
  const NavigationScreen({super.key});

  @override
  State<NavigationScreen> createState() => _NavigationScreenState();
}

class _NavigationScreenState extends State<NavigationScreen> {
  int _selectedIndex = 0;
  static final List<Widget> _widgetOptions = <Widget>[
    HomeScreen(),
    Center(
      child: Consumer(
        builder: (context, ref, child) {
          return IconButton(
              onPressed: () {
                ref.read(authStateProvider.notifier).signOut();
              },
              icon: Icon(Icons.logout));
        },
      ),
    ),
    Center(
      child: Text("misfavores"),
    ),
  ];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: _widgetOptions.elementAt(_selectedIndex),
      ),
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

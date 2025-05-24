import 'package:flutter/material.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/user/models/user_model.dart';
import 'package:senefavores/views/navigation/navigation_screen.dart';

Widget buildCustomDialog(
    BuildContext context, UserModel requesterUser, FavorModel favor) {
  return AlertDialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    backgroundColor: Colors.amber,
    content: Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        const Text(
          "¡Favor aceptado!",
          style: TextStyle(
            fontWeight: FontWeight.bold,
            fontSize: 20,
          ),
        ),
        const SizedBox(height: 10),
        const Text(
          "Comunícate con ",
          style: TextStyle(fontSize: 16),
        ),
        Text(
          requesterUser.name ?? 'Nombre usuario',
          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 10),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.phone, color: Colors.black),
            SizedBox(width: 5),
            Text(
              requesterUser.phone ?? 'Teléfono usuario',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
          ],
        ),
        const SizedBox(height: 20),
        ElevatedButton(
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.black,
            foregroundColor: Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
          ),
          onPressed: () {
            Navigator.of(context).pop(); // close the dialog first
            Future.microtask(() {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => NavigationScreen(initialIndex: 2),
                ),
              );
            });
          },
          child: const Text("Ir a Mis Favores"),
        ),
      ],
    ),
  );
}

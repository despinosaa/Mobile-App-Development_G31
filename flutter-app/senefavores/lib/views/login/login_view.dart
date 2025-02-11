import 'package:flutter/material.dart';

class LoginView extends StatelessWidget {
  const LoginView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SizedBox(
        width: double.infinity,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          children: [
            Column(
              children: [
                Image.asset(
                  'assets/images/senefavores_logo.png',
                  height: 250,
                  width: 250,
                ),
                Text("Senefavores"),
              ],
            ),
            TextButton(
              style: TextButton.styleFrom(
                side: BorderSide(color: Colors.black, width: 1),
                padding:
                    const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text('Iniciar sesión con Microsoft  '),
                  Image.asset(
                    'assets/images/microsoft_logo.png',
                    height: 30,
                    width: 30,
                  ),
                ],
              ),
              onPressed: () {},
            ),
          ],
        ),
      ),
    );
  }
}

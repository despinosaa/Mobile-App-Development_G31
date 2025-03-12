import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/auth/provider/auth_state_notifier_provider.dart';

class LoginView extends ConsumerWidget {
  const LoginView({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      body: SizedBox(
        width: double.infinity,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            Column(
              children: [
                Image.asset(
                  'assets/images/senefavores_logo.png',
                  height: 250,
                  width: 250,
                ),
                Text(
                  "Senefavores",
                  style: GoogleFonts.oswald(
                    fontSize: 50,
                  ),
                ),
              ],
            ),
            Padding(
              padding: const EdgeInsets.only(bottom: 30),
              child: TextButton(
                style: TextButton.styleFrom(
                  side: BorderSide(color: Colors.orange, width: 1.5),
                  padding:
                      const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text('Iniciar sesión con Microsoft  ',
                        style: TextStyle(color: Colors.black)),
                    Image.asset(
                      'assets/images/microsoft_logo.png',
                      height: 30,
                      width: 30,
                    ),
                  ],
                ),
                onPressed: () async {
                  await ref
                      .read(authStateProvider.notifier)
                      .signInWithMicrosoft();
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class LoginView extends StatefulWidget {
  const LoginView({super.key});

  @override
  State<LoginView> createState() => _LoginViewState();
}

class _LoginViewState extends State<LoginView> {
  @override
  Widget build(BuildContext context) {
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
                  final supabase = Supabase.instance.client;
                  //await supabase.auth.signOut();
                  await supabase.auth.signInWithOAuth(
                    OAuthProvider.azure,
                    authScreenLaunchMode: LaunchMode.externalApplication,
                    redirectTo:
                        'https://kebumzcxttyquorhiicf.supabase.co/auth/v1/callback',
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}

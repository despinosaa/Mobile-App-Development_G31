import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class LoginView extends StatefulWidget {
  const LoginView({super.key});

  @override
  State<LoginView> createState() => _LoginViewState();
}

class _LoginViewState extends State<LoginView> {
  @override
  void initState() {
    super.initState();
    handleRedirectResult();
  }

  void microsoftSignIn() async {
    final provider = OAuthProvider('microsoft.com');

    provider.setCustomParameters({
      "prompt": "select_account",
      "tenant": "common", // Use your tenant ID if needed
      "response_mode": "form_post" // Ensures a proper POST request
    });

    try {
      await FirebaseAuth.instance.signInWithRedirect(provider);
      print("Redirecting to Microsoft login...");
    } catch (e) {
      print("Login failed: $e");
    }
  }

  void handleRedirectResult() async {
    try {
      final userCredential = await FirebaseAuth.instance.getRedirectResult();
      if (userCredential.user != null) {
        print("Login successful: ${userCredential.user?.email}");
        Navigator.pushReplacementNamed(
            context, '/home'); // Redirect to home screen
      } else {
        print("No user signed in after redirect.");
      }
    } catch (e) {
      print("Error handling redirect: $e");
    }
  }

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
                  FirebaseAuth.instance.signOut();
                  try {
                    print("STARTING LOGIN WITH MICROSOFT");

                    final provider = OAuthProvider('microsoft.com');

                    provider.setCustomParameters({
                      "prompt": "select_account",
                      "tenant": "common",
                    });

                    final userCredential = await FirebaseAuth.instance
                        .signInWithProvider(provider);

                    print("Login successful: ${userCredential.user?.email}");
                    print("User: ${userCredential.user}");
                  } catch (e) {
                    print("Login failed: $e");
                  }
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}

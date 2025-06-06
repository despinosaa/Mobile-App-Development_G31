import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/auth/provider/auth_state_notifier_provider.dart';
import 'package:senefavores/state/connectivity/connectivity_provider.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:senefavores/utils/logger.dart';

class LoginView extends ConsumerWidget {
  const LoginView({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final connectivity = ref.watch(connectivityProvider).value;
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
                  final start = DateTime.now();

                  try {
                    if (connectivity == ConnectivityResult.none) {
                      ref.read(snackbarProvider).showSnackbar(
                          "Sin conexion a internet",
                          isError: true);
                    } else {
                      await ref
                          .read(authStateProvider.notifier)
                          .signInWithMicrosoft();
                    }

                    final duration =
                        DateTime.now().difference(start).inMilliseconds;
                    await AppLogger.logResponseTime(
                      screen: 'LoginView',
                      responseTimeMs: duration,
                    );
                  } catch (e) {
                    await AppLogger.logCrash(
                      screen: 'LoginView',
                      crashInfo: e.toString(),
                    );
                    rethrow; // Let it continue if needed
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

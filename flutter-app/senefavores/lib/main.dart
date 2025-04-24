import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/core/theme.dart';
import 'package:senefavores/state/auth/models/auth_result.dart';
import 'package:senefavores/state/auth/provider/auth_state_notifier_provider.dart';
import 'package:senefavores/state/loading/is_loading_provider.dart';
import 'package:senefavores/state/snackbar/models/snackbar_message_model.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_notification_provider.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:senefavores/state/user/providers/auth_watcher_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:senefavores/views/components/loading_screen.dart';
import 'package:senefavores/views/login/login_view.dart';
import 'package:senefavores/views/navigation/navigation_screen.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'dart:async';
import 'package:senefavores/utils/logger.dart';

void main() {
  runZonedGuarded(() async {
    WidgetsFlutterBinding.ensureInitialized();

    await Supabase.initialize(
      url: 'https://kebumzcxttyquorhiicf.supabase.co',
      anonKey:
          'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtlYnVtemN4dHR5cXVvcmhpaWNmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDE2NDM1MDQsImV4cCI6MjA1NzIxOTUwNH0.PiAnATAnWk_7Brz6XzZqQMkaCoGOItFGKhy1EZ8OnVg',
    );

    FlutterError.onError = (details) {
      FlutterError.presentError(details);
      AppLogger.logCrash(
        screen: 'global',
        crashInfo: details.exceptionAsString(),
      );
    };

    runApp(const ProviderScope(child: MyApp()));
  }, (error, stack) {
    AppLogger.logCrash(
      screen: 'global',
      crashInfo: error.toString(),
    );
  });
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter Demo',
      theme: AppTheme.lightTheme,
      home: SafeArea(
        child: Consumer(
          builder: (context, ref, child) {
            ref.watch(authWatcherProvider);
            ref.read(snackbarProvider).setContext(context);
            ref.listen<SnackbarMessageModel>(snackbarNotificationProvider,
                (previous, next) {
              if (next.message.isNotEmpty) {
                ref
                    .read(snackbarProvider)
                    .showSnackbar(next.message, isError: next.isError);
              }
            });

            ref.listen<bool>(isLoadingProvider, (previous, next) {
              if (next) {
                LoadingScreen.instance()
                    .show(context: context, text: 'Loading...');
              } else {
                LoadingScreen.instance().hide();
              }
            });

            final authState = ref.watch(authStateProvider);

            if (authState.result == AuthResult.loggedIn) {
              return const NavigationScreen();
            } else {
              return const LoginView();
            }
          },
        ),
      ),
    );
  }
}

import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/auth/models/auth_result.dart';
import 'package:senefavores/state/auth/models/auth_status.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class AuthStateNotifier extends StateNotifier<AuthStatus> {
  final _supabase = Supabase.instance.client;

  AuthStateNotifier()
      : super(const AuthStatus(result: null, isLoading: false, userId: null));

  Future<void> signInWithMicrosoft() async {
    _supabase.auth.signOut();
    state = state.copyWith(isLoading: true);

    await _supabase.auth.signInWithOAuth(
      OAuthProvider.azure,
      authScreenLaunchMode: LaunchMode.externalApplication,
      redirectTo: 'io.supabase.flutter://login-callback',
      scopes: 'openid profile email User.Read',
      queryParams: {
        'prompt': 'select_account',
      },
    );

    _supabase.auth.onAuthStateChange.listen((data) {
      final session = data.session;
      final user = session?.user;

      if (user != null) {
        final userdomain = user.email!.split('@')[1];
        if (userdomain == 'uniandes.edu.co') {
          state = state.copyWith(
            result: AuthResult.loggedIn,
            userId: user.id,
            isLoading: false,
          );
        } else {
          _supabase.auth.signOut();
          state = state.copyWith(
            result: AuthResult.loggedOut,
            isLoading: false,
          );
        }
        state = state.copyWith(isLoading: false);
      }
    });
  }

  Future<void> signOut() async {
    state = state.copyWith(isLoading: true);

    await _supabase.auth.signOut();

    _supabase.auth.onAuthStateChange.listen((data) {
      final session = data.session;
      final user = session?.user;

      if (user == null) {
        state = state.copyWith(
          result: AuthResult.loggedOut,
          userId: null,
          isLoading: false,
        );
      }
    });
    state = state.copyWith(isLoading: false);
  }
}

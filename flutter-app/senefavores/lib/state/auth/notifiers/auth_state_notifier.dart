// lib/state/auth/notifiers/auth_state_notifier.dart

import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/core/providers.dart';
import 'package:senefavores/services/session_storage.dart';
import 'package:senefavores/state/auth/models/auth_result.dart';
import 'package:senefavores/state/auth/models/auth_status.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class AuthStateNotifier extends StateNotifier<AuthStatus> {
  final Ref _ref; // ← store the Ref
  final _supabase = Supabase.instance.client;

  AuthStateNotifier(this._ref) // ← take it in
      : super(const AuthStatus(
          result: AuthResult.none,
          isLoading: false,
          userId: null,
        )) {
    _restoreSession();
  }

  Future<void> _restoreSession() async {
    state = state.copyWith(isLoading: true);

    final storage = _ref.read(sessionStorageProvider);
    final access = await storage.readAccess();
    final refresh = await storage.readRefresh();

    if (access != null && _supabase.auth.currentUser != null) {
      state = state.copyWith(
        result: AuthResult.loggedIn,
        userId: _supabase.auth.currentUser!.id,
        isLoading: false,
      );
      return;
    }

    await storage.clear();
    state = state.copyWith(
      result: AuthResult.none,
      userId: null,
      isLoading: false,
    );
  }

  Future<void> signInWithMicrosoft() async {
    state = state.copyWith(isLoading: true, result: AuthResult.none);
    await _supabase.auth.signOut();

    await _supabase.auth.signInWithOAuth(
      OAuthProvider.azure,
      authScreenLaunchMode: LaunchMode.externalApplication,
      redirectTo: 'io.supabase.flutter://login-callback',
      scopes: 'openid profile email User.Read',
      queryParams: {'prompt': 'select_account'},
    );

    _supabase.auth.onAuthStateChange.listen((data) async {
      final session = data.session!; // ← unwrap safely
      final user = session.user!; // ← unwrap safely

      final domain = user.email!.split('@')[1];
      if (domain == 'uniandes.edu.co') {
        // … your existing “insert if new” logic …

        // **persist** the tokens
        await _ref.read(sessionStorageProvider).save(
              access: session.accessToken!,
              refresh: session.refreshToken!,
            );

        state = state.copyWith(
          result: AuthResult.loggedIn,
          userId: user.id,
          isLoading: false,
        );
      } else {
        await _supabase.auth.signOut();
        state = state.copyWith(
          result: AuthResult.loggedOut,
          isLoading: false,
        );
      }
    });
  }

  Future<void> signOut() async {
    state = state.copyWith(isLoading: true);
    await _supabase.auth.signOut();
    await _ref.read(sessionStorageProvider).clear(); // ← clear storage

    state = state.copyWith(
      result: AuthResult.none,
      userId: null,
      isLoading: false,
    );
  }

  Future<void> signOutWithNoConnection() async {
    state = state.copyWith(isLoading: true);
    await _ref.read(sessionStorageProvider).clear();
    state = state.copyWith(
      result: AuthResult.none,
      userId: null,
      isLoading: false,
    );
  }
}

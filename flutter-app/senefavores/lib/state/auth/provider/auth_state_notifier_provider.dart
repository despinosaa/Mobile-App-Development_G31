import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/auth/models/auth_status.dart';
import 'package:senefavores/state/auth/notifiers/auth_state_notifier.dart';
import 'package:senefavores/core/providers.dart';

final authStateProvider = StateNotifierProvider<AuthStateNotifier, AuthStatus>(
  (ref) => AuthStateNotifier(ref),
);

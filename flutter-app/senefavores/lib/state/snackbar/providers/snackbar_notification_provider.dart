import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/auth/models/auth_result.dart';
import 'package:senefavores/state/auth/models/auth_status.dart';
import 'package:senefavores/state/auth/provider/auth_state_notifier_provider.dart';
import 'package:senefavores/state/snackbar/models/snackbar_message_model.dart';

final snackbarNotificationProvider = Provider<SnackbarMessageModel>((ref) {
  final AuthStatus authState = ref.watch(authStateProvider);

  if (authState.result == AuthResult.loggedIn) {
    return SnackbarMessageModel(
      message: '✅ Successfully logged in!',
      isError: false,
    );
  }
  if (authState.result == AuthResult.cancelled) {
    return SnackbarMessageModel(
      message: '❌ Login was canceled or failed.',
      isError: true,
    );
  }
  if (authState.result == AuthResult.loggedOut) {
    return SnackbarMessageModel(
      message: '❌ Login failed: Only @uniandes.edu.co emails are allowed.',
      isError: true,
    );
  } else {
    return SnackbarMessageModel(
      message: '',
      isError: false,
    );
  }
});

import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/auth/provider/auth_state_notifier_provider.dart';

final isLoadingProvider = Provider<bool>((ref) {
  final authstate = ref.watch(authStateProvider);

  return authstate.isLoading;
});

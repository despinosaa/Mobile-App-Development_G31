import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/auth/provider/auth_state_notifier_provider.dart';
import 'package:senefavores/state/favors/providers/upload_favor_state_notifier_provider.dart.dart';

final isLoadingProvider = Provider<bool>((ref) {
  final authstate = ref.watch(authStateProvider);
  final uploadFavor = ref.watch(uploadFavorStateNotifierProvider);

  return authstate.isLoading || uploadFavor;
});

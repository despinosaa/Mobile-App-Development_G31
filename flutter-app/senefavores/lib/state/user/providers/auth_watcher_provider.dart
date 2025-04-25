import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/auth/provider/auth_state_notifier_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';

final authWatcherProvider = Provider.autoDispose((ref) {
  ref.listen(authStateProvider, (previous, next) {
    if (previous != next) {
      ref.read(currentUserNotifierProvider.notifier).refreshUser();
    }
  });
});

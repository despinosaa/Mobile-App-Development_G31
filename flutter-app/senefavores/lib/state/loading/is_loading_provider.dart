import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/auth/provider/auth_state_notifier_provider.dart';
import 'package:senefavores/state/favors/providers/accept_favor_state_notifier_provider.dart';
import 'package:senefavores/state/favors/providers/upload_favor_state_notifier_provider.dart.dart';
import 'package:senefavores/state/location/providers/user_location_state_notifier_provider.dart';

final isLoadingProvider = Provider<bool>((ref) {
  final authstate = ref.watch(authStateProvider);
  final uploadFavor = ref.watch(uploadFavorStateNotifierProvider);
  final acceptFavor = ref.watch(acceptFavorProvider);
  final userLocation = ref.watch(userLocationProvider);

  return authstate.isLoading || uploadFavor || acceptFavor || userLocation;
});

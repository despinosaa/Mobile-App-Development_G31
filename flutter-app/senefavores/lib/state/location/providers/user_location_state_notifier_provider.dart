import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/location/notifiers/user_location_state_notifer.dart';

final userLocationProvider =
    StateNotifierProvider<UserLocationStateNotifier, bool>(
  (ref) => UserLocationStateNotifier(),
);

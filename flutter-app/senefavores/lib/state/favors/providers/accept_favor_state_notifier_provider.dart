import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/favors/notifiers/accept_favor_state_notifier.dart';

final acceptFavorProvider = StateNotifierProvider<AcceptFavorNotifier, bool>(
  (ref) => AcceptFavorNotifier(),
);

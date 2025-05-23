import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/favors/notifiers/cancel_favor_state_notifier.dart';

final cancelFavorProvider = StateNotifierProvider<CancelFavorNotifier, bool>(
  (ref) => CancelFavorNotifier(),
);

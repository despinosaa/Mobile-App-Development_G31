import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/favors/notifiers/complete_favor_sate_notifier.dart';

final completeFavorProvider =
    StateNotifierProvider<CompleteFavorNotifier, bool>(
  (ref) => CompleteFavorNotifier(),
);

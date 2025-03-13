import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/favors/notifiers/upload_favor_state_notifier.dart';

final uploadFavorStateNotifierProvider =
    StateNotifierProvider<UploadFavorStateNotifier, bool>(
  (ref) => UploadFavorStateNotifier(),
);

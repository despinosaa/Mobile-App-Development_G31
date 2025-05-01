import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/services/session_storage.dart';

final sessionStorageProvider = Provider<SessionStorage>((ref) {
  return SessionStorage();
});

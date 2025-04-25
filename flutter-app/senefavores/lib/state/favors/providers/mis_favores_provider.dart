import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/connectivity/connectivity_provider.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/utils/logger.dart';

final favorsRequestedByUserProvider = StreamProvider.family
    .autoDispose<List<FavorModel>, String>((ref, userId) async* {
  final supabase = Supabase.instance.client;
  final box = await Hive.openBox('favorsBox');
  final connectivity = ref.watch(connectivityProvider).value;
  final cacheKey = 'requestedFavors_$userId';

  if (connectivity == ConnectivityResult.none) {
    // Offline: yield cached data
    final cached = box.get(cacheKey, defaultValue: []);
    ref.read(snackbarProvider).showSnackbar(
          "No internet connection. Showing cached favors.",
          isError: true,
        );
    yield List<Map<String, dynamic>>.from(cached)
        .map((e) => FavorModel.fromJson(e))
        .toList();
    return;
  }

  // Online: stream from Supabase
  final start = DateTime.now();
  yield* supabase
      .from('favors')
      .stream(primaryKey: ['id'])
      .eq('request_user_id', userId)
      .order('created_at', ascending: false)
      .map((data) {
        final duration = DateTime.now().difference(start).inMilliseconds;
        AppLogger.logResponseTime(
            screen: 'MisFavoresView (Solicitados)', responseTimeMs: duration);

        // Cache the data
        box.put(cacheKey, data);

        return data.map((favor) => FavorModel.fromJson(favor)).toList();
      });
});

final favorsAcceptedByUserProvider = StreamProvider.family
    .autoDispose<List<FavorModel>, String>((ref, userId) async* {
  final supabase = Supabase.instance.client;
  final box = await Hive.openBox('favorsBox');
  final connectivity = ref.watch(connectivityProvider).value;
  final cacheKey = 'acceptedFavors_$userId';

  if (connectivity == ConnectivityResult.none) {
    // Offline: yield cached data
    final cached = box.get(cacheKey, defaultValue: []);
    ref.read(snackbarProvider).showSnackbar(
          "No internet connection. Showing cached favors.",
          isError: true,
        );
    yield List<Map<String, dynamic>>.from(cached)
        .map((e) => FavorModel.fromJson(e))
        .toList();
    return;
  }

  // Online: stream from Supabase
  final start = DateTime.now();
  yield* supabase
      .from('favors')
      .stream(primaryKey: ['id'])
      .eq('accept_user_id', userId)
      .order('created_at', ascending: false)
      .map((data) {
        final duration = DateTime.now().difference(start).inMilliseconds;
        AppLogger.logResponseTime(
            screen: 'MisFavoresView (Aceptados)', responseTimeMs: duration);

        // Cache the data
        box.put(cacheKey, data);

        return data.map((favor) => FavorModel.fromJson(favor)).toList();
      });
});

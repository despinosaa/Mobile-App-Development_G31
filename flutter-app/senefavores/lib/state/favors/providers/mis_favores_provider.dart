import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/utils/logger.dart';

final favorsRequestedByUserProvider =
    StreamProvider.family.autoDispose<List<FavorModel>, String>((ref, userId) {
  final supabase = Supabase.instance.client;
  final start = DateTime.now();

  return supabase
      .from('favors')
      .stream(primaryKey: ['id'])
      .eq('request_user_id', userId)
      .order('created_at', ascending: false)
      .map((data) {
        final duration = DateTime.now().difference(start).inMilliseconds;
        AppLogger.logResponseTime(
          screen: 'MisFavoresView (Solicitados)',
          responseTimeMs: duration,
        );
        return data.map((favor) => FavorModel.fromJson(favor)).toList();
      });
});

final favorsAcceptedByUserProvider =
    StreamProvider.family.autoDispose<List<FavorModel>, String>((ref, userId) {
  final supabase = Supabase.instance.client;
  final start = DateTime.now();

  return supabase
      .from('favors')
      .stream(primaryKey: ['id'])
      .eq('accept_user_id', userId)
      .order('created_at', ascending: false)
      .map((data) {
        final duration = DateTime.now().difference(start).inMilliseconds;
        AppLogger.logResponseTime(
          screen: 'MisFavoresView (Aceptados)',
          responseTimeMs: duration,
        );
        return data.map((favor) => FavorModel.fromJson(favor)).toList();
      });
});

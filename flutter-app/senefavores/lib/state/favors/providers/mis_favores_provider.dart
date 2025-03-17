import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';

/// Favors requested by the given user (sorted by favor_time DESC).
final favorsRequestedByUserProvider =
    StreamProvider.family.autoDispose<List<FavorModel>, int>((ref, userId) {
  final supabase = Supabase.instance.client;
  return supabase
      .from('favors')
      .stream(primaryKey: ['id'])
      .eq('request_user_id', userId)
      .order('favor_time', ascending: false)
      .map((data) => data.map((favor) => FavorModel.fromJson(favor)).toList());
});

final favorsAcceptedByUserProvider =
    StreamProvider.family.autoDispose<List<FavorModel>, int>((ref, userId) {
  final supabase = Supabase.instance.client;
  return supabase
      .from('favors')
      .stream(primaryKey: ['id'])
      .eq('accept_user_id', userId)
      .order('favor_time', ascending: false)
      .map((data) => data.map((favor) => FavorModel.fromJson(favor)).toList());
});

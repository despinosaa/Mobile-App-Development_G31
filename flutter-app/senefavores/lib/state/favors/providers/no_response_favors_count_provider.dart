import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

final noResponseFavorsCountProvider =
    FutureProvider.autoDispose<int>((ref) async {
  final supabase = Supabase.instance.client;

  final now = DateTime.now();
  final since = now.subtract(const Duration(hours: 24)).toIso8601String();

  final raw = await supabase
      .from('favors')
      .select('id, created_at, accept_user_id')
      .gte('created_at', since);

  final rows = (raw as List<dynamic>)
      .cast<Map<String, dynamic>>()
      .where((r) => r['accept_user_id'] == null);

  return rows.length;
});

import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

final favorAverageAcceptanceTimeProvider =
    StreamProvider.family.autoDispose<double, String>((ref, category) {
  final supabase = Supabase.instance.client;

  if (category.isEmpty) return Stream.value(0.0);

  final stream = supabase
      .from('favors')
      .stream(primaryKey: ['id'])
      .eq('category', category)
      .order('created_at', ascending: false);
  return stream.map((rows) {
    final accepted = rows
        .where((f) => f['accept_user_id'] != null && f['favor_time'] != null)
        .toList();

    if (accepted.isEmpty) return 0.0;

    final totalSec = accepted.fold<int>(0, (sum, f) {
      final created = DateTime.parse(f['created_at'] as String);
      final acceptedAt = DateTime.parse(f['favor_time'] as String);
      return sum + acceptedAt.difference(created).inSeconds;
    });

    return totalSec / accepted.length / 60.0;
  });
});

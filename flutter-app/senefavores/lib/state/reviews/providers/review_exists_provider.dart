import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

typedef ReviewKey = ({String favorId, String reviewerId});

final reviewExistsProvider = StreamProvider.family<bool, ReviewKey>((ref, key) {
  final supabase = Supabase.instance.client;

  final allRowsStream = supabase.from('reviews').stream(primaryKey: ['id']);

  return allRowsStream.map((rows) => rows.any((row) =>
      row['favor_id'] == key.favorId && row['reviewer_id'] == key.reviewerId));
});

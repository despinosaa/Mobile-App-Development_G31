import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:senefavores/state/reviews/models/review_model.dart';

final userReviewsProvider =
    FutureProvider.family<List<ReviewModel>, String>((ref, userId) async {
  final supabase = Supabase.instance.client;

  final response = await supabase
      .from('reviews')
      .select()
      .eq('reviewed_id', userId)
      .order('created_at', ascending: false);

  if (response.isEmpty) {
    return [];
  }

  return response.map((review) => ReviewModel.fromJson(review)).toList();
});

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:senefavores/state/reviews/models/review_model.dart';
import 'package:senefavores/utils/local_database.dart';

final userReviewsProvider =
FutureProvider.family<List<ReviewModel>, String>((ref, userId) async {
  try {
    final supabase = Supabase.instance.client;
    final conn = await Connectivity().checkConnectivity();

    if (conn == ConnectivityResult.none) {
      // offline → local cache
      return await LocalDatabase.instance.getCachedReviews(userId);
    }

    // online → Supabase
    final resp = await supabase
        .from('reviews')
        .select()
        .eq('reviewed_id', userId)
        .order('created_at', ascending: false);

    final reviews = (resp as List)
        .map((r) => ReviewModel.fromJson(r as Map<String, dynamic>))
        .toList();

    // update cache
    await LocalDatabase.instance.clearCachedReviews(userId);
    await LocalDatabase.instance.cacheReviews(reviews);

    return reviews;
  } catch (e) {
    // on error, fallback to cache
    return await LocalDatabase.instance.getCachedReviews(userId);
  }
});

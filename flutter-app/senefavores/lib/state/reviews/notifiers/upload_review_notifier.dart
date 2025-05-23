import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:uuid/uuid.dart';
import '../models/review_model.dart';

class UploadReviewNotifier extends StateNotifier<bool> {
  UploadReviewNotifier() : super(false);

  Future<bool> uploadReview({
    required String title,
    required String description,
    required double stars,
    required String reviewerId,
    required String reviewedId,
    required String favorId,
  }) async {
    final supabase = Supabase.instance.client;
    final model = ReviewModel(
      id: const Uuid().v4(),
      title: title,
      description: description,
      stars: stars,
      createdAt: DateTime.now(),
      reviewerId: reviewerId,
      reviewedId: reviewedId,
      favorId: favorId,
    );

    try {
      state = true;
      await supabase.from('reviews').insert(model.toJson());
      return true;
    } catch (e, st) {
      return false;
    } finally {
      state = false;
    }
  }
}

import 'package:hooks_riverpod/hooks_riverpod.dart';
import '../notifiers/upload_review_notifier.dart';

final uploadReviewProvider = StateNotifierProvider<UploadReviewNotifier, bool>(
  (ref) => UploadReviewNotifier(),
);

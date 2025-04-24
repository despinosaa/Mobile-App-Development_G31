import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

final favorAverageAcceptanceTimeProvider =
    StreamProvider.family.autoDispose<double, String>((ref, category) async* {
  final supabase = Supabase.instance.client;

  if (category.isEmpty) {
    yield 0.0;
    return;
  }

  try {
    final totalFavorsStream = supabase
        .from('favors')
        .stream(primaryKey: ['id'])
        .eq('category', category)
        .order('created_at', ascending: false)
        .map((List<Map<String, dynamic>> data) {
          return data.where((favor) => favor['category'] == category).toList();
        });

    final acceptedFavorsStream = supabase
        .from('favors')
        .stream(primaryKey: ['id'])
        .eq('category', category)
        .order('created_at', ascending: false)
        .map((List<Map<String, dynamic>> data) {
          return data
              .where((favor) =>
                  favor['accept_user_id'] != null &&
                  favor['category'] == category &&
                  favor['favor_time'] != null)
              .toList();
        });

    await for (final totalFavors in totalFavorsStream) {
      await for (final acceptedFavors in acceptedFavorsStream) {
        if (acceptedFavors.isEmpty) {
          yield 0.0;
        } else {
          int totalTime = 0;
          int acceptedCount = 0;

          for (var favor in acceptedFavors) {
            final createdAt = favor['created_at'] as String;
            final favorTime = favor['favor_time'] as String;

            final createdAtDate = DateTime.parse(createdAt);
            final favorTimeDate = DateTime.parse(favorTime);

            final timeDiffInSeconds =
                favorTimeDate.difference(createdAtDate).inSeconds;
            totalTime += timeDiffInSeconds;
            acceptedCount++;
          }

          final averageAcceptanceTime = totalTime / acceptedCount;

          final averageAcceptanceTimeInMinutes = averageAcceptanceTime / 60.0;

          yield averageAcceptanceTimeInMinutes;
        }
      }
    }
  } catch (e) {
    print('Error fetching favor data: $e');
    yield 0.0;
  }
});

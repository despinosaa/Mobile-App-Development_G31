import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:senefavores/utils/logger.dart';

final favorAverageAcceptanceTimeProvider =
    StreamProvider.family.autoDispose<double, String>((ref, category) async* {
  final supabase = Supabase.instance.client;

  if (category.isEmpty) {
    // Handle case when category is empty
    yield 0.0;
    return;
  }

  try {
    // Stream of all favors for the selected category
    final totalFavorsStream = supabase
        .from('favors')
        .stream(primaryKey: ['id'])
        .eq('category', category)
        .order('created_at', ascending: false)
        .map((List<Map<String, dynamic>> data) {
          return data.where((favor) => favor['category'] == category).toList();
        });

    // Stream of accepted favors for the selected category (with accept_user_id not null)
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
                  favor['favor_time'] != null) // Ensure favor_time exists
              .toList();
        });

    // Combine both streams and calculate the average acceptance time
    await for (final totalFavors in totalFavorsStream) {
      await for (final acceptedFavors in acceptedFavorsStream) {
        // Check if there are any accepted favors to prevent division by zero
        if (acceptedFavors.isEmpty) {
          yield 0.0; // If no accepted favors exist in this category, the rate is 0%
        } else {
          // Calculate total time differences for accepted favors
          int totalTime = 0;
          int acceptedCount = 0;

          for (var favor in acceptedFavors) {
            final createdAt = favor['created_at'] as String;
            final favorTime = favor['favor_time'] as String;

            final createdAtDate = DateTime.parse(createdAt);
            final favorTimeDate = DateTime.parse(favorTime);

            // Calculate the time difference in seconds (you can also use other units)
            final timeDiffInSeconds =
                favorTimeDate.difference(createdAtDate).inSeconds;
            totalTime += timeDiffInSeconds;
            acceptedCount++;
          }

          // Calculate average acceptance time
          final averageAcceptanceTime = totalTime / acceptedCount;

          // Convert time to a more readable format (optional, here in seconds)
          final averageAcceptanceTimeInMinutes = averageAcceptanceTime / 60.0;

          // Yield the result
          yield averageAcceptanceTimeInMinutes;
        }
      }
    }
  } catch (e) {
    // Handle error case and log it
    print(
        'Error fetching favor data: $e'); // Replace this with your actual logger
    yield 0.0; // Return 0.0 in case of an error
  }
});

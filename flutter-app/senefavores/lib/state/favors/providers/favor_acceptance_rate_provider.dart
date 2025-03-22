import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:senefavores/utils/logger.dart';

final favorAcceptanceRateProvider =
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
                  favor['category'] == category)
              .toList();
        });

    // Combine both streams and calculate acceptance rate
    await for (final totalFavors in totalFavorsStream) {
      await for (final acceptedFavors in acceptedFavorsStream) {
        // Check if total favors is 0 to prevent division by zero
        if (totalFavors.isEmpty) {
          yield 0.0; // If no favors exist in this category, the rate is 0%
        } else {
          final acceptanceRate =
              (acceptedFavors.length / totalFavors.length) * 100;
          yield acceptanceRate;
        }
      }
    }
  } catch (e) {
    // Handle error case and log it
    print(
        'Error fetching favor data: $e'); // Replace this with your actual logger
    yield 0.0; // Return 0% in case of an error
  }
});

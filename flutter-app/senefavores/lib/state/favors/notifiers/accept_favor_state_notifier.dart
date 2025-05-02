import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class AcceptFavorNotifier extends StateNotifier<bool> {
  AcceptFavorNotifier() : super(false);

  Future<bool> acceptFavor(
      {required String favorId, required String userId}) async {
    final supabase = Supabase.instance.client;

    try {
      state = true;

      final List response = await supabase
          .from('favors')
          .update({
            'accept_user_id': userId,
            'favor_time': DateTime.now().toIso8601String(),
            'status': 'accepted',
          })
          .eq('id', favorId)
          .select();

      state = false;

      return response.isNotEmpty;
    } catch (e) {
      state = false;
      print("Error accepting favor: $e");
      return false;
    }
  }
}

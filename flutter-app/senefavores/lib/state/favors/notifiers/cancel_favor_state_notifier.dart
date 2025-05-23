import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class CancelFavorNotifier extends StateNotifier<bool> {
  CancelFavorNotifier() : super(false);

  Future<bool> cancelFavor({required String favorId}) async {
    final supabase = Supabase.instance.client;

    try {
      state = true;

      final List response = await supabase
          .from('favors')
          .update({
            'status': 'cancelled',
          })
          .eq('id', favorId)
          .select();

      state = false;

      return response.isNotEmpty;
    } catch (e) {
      state = false;
      print("Error cancelling favor: $e");
      return false;
    }
  }
}

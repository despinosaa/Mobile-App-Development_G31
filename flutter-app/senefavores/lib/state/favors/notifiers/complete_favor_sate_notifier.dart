import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class CompleteFavorNotifier extends StateNotifier<bool> {
  CompleteFavorNotifier() : super(false);

  Future<bool> completeFavor({required String favorId}) async {
    final supabase = Supabase.instance.client;

    try {
      state = true;

      final List response = await supabase
          .from('favors')
          .update({
            'status': 'done',
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

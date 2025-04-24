import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';

class UploadFavorStateNotifier extends StateNotifier<bool> {
  UploadFavorStateNotifier() : super(false);

  Future<bool> uploadFavor({required FavorModel favor}) async {
    final supabase = Supabase.instance.client;

    try {
      state = true;

      await supabase.from('favors').insert(favor.toJson());

      state = false;
      return true;
    } catch (e) {
      state = false;
      return false;
    }
  }
}

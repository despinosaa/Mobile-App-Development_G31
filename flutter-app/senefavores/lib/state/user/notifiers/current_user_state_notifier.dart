import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/user/models/user_model.dart';
import 'package:senefavores/utils/local_database.dart';

class CurrentUserStateNotifier extends StateNotifier<UserModel?> {
  CurrentUserStateNotifier(): super(null) {
    fetchCurrentUser();
  }

  /// Called on sign-in / auth changes
  Future<void> fetchCurrentUser() async {
    try {
      final supabase = Supabase.instance.client;
      final authUser = supabase.auth.currentUser;
      if (authUser == null) {
        state = null;
        return;
      }

      // 1. check connectivity
      final conn = await Connectivity().checkConnectivity();
      if (conn == ConnectivityResult.none) {
        // → offline: load from cache
        state = await LocalDatabase.instance.getCachedUser(authUser.id);
        return;
      }

      // 2. online: fetch from Supabase
      final resp = await supabase
          .from('clients')
          .select()
          .eq('id', authUser.id)
          .maybeSingle();

      if (resp != null) {
        final u = UserModel.fromJson(resp as Map<String, dynamic>);
        state = u;
        // cache
        await LocalDatabase.instance.cacheUser(u);
      } else {
        state = null;
      }
    } catch (e) {
      // on error, fall back to cache
      final authUser = Supabase.instance.client.auth.currentUser;
      if (authUser != null) {
        state = await LocalDatabase.instance.getCachedUser(authUser.id);
      } else {
        state = null;
      }
    }
  }

  void updatePhone(String phone) {
    if (state != null) {
      final updated = state!.copyWith(phone: phone);
      state = updated;
      // update cache
      LocalDatabase.instance.cacheUser(updated);
    }
  }

  Future<void> clearUser() async {
    state = null;
  }

  Future<void> refreshUser() => fetchCurrentUser();
}

import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:senefavores/state/user/models/user_model.dart';

class CurrentUserStateNotifier extends StateNotifier<UserModel?> {
  CurrentUserStateNotifier() : super(null) {
    fetchCurrentUser();
  }

  void updatePhone(String phone) {
    if (state != null) {
      state = state!.copyWith(phone: phone);
    }
  }

  Future<void> fetchCurrentUser() async {
    try {
      final supabase = Supabase.instance.client;
      final user = supabase.auth.currentUser;

      if (user == null) {
        state = null;
        return;
      }

      final Map<String, dynamic>? response = await supabase
          .from('clients')
          .select()
          .eq('email', user.email!)
          .maybeSingle();

      if (response != null) {
        state = UserModel.fromJson(response);
      } else {
        state = null;
      }
    } catch (e) {
      state = null;
    }
  }

  void setUser(UserModel user) {
    state = user;
  }

  void clearUser() {
    state = null;
  }

  Future<void> refreshUser() async {
    await fetchCurrentUser();
  }
}

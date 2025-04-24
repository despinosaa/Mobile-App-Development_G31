import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/user/models/user_model.dart';
import 'package:senefavores/state/user/notifiers/current_user_state_notifier.dart';

// final currentUserProvider = FutureProvider<UserModel>((ref) async {
//   final supabase = Supabase.instance.client;
//   final user = supabase.auth.currentUser;

//   final Map<String, dynamic>? response = await supabase
//       .from('users')
//       .select()
//       .eq('email', user!.email!)
//       .maybeSingle();

//   return UserModel.fromJson(response!);
// });

final currentUserNotifierProvider =
    StateNotifierProvider<CurrentUserStateNotifier, UserModel?>(
  (ref) => CurrentUserStateNotifier(),
);

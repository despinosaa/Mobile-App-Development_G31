import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/user/models/user_model.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

final currentUserProvider = FutureProvider<UserModel>((ref) async {
  final supabase = Supabase.instance.client;
  final user = supabase.auth.currentUser;
  final response = await supabase
      .from('users')
      .select()
      .eq('email', user!.email!)
      .maybeSingle();

  if (user == null) {
    throw Exception("User not found");
  }

  return UserModel.fromJson(user.toJson());
});

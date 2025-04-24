import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/user/models/user_model.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

final userProvider = StreamProvider.family<UserModel, String>((ref, userId) {
  final supabase = Supabase.instance.client;

  return supabase
      .from('clients')
      .stream(primaryKey: ['id'])
      .eq('id', userId)
      .map((data) {
        if (data.isEmpty) {
          throw Exception("User not found");
        }
        return UserModel.fromJson(data.first);
      });
});

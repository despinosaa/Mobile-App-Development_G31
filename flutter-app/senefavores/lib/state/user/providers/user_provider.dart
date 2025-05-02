import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/user/models/user_model.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:senefavores/state/connectivity/connectivity_provider.dart';

final userProvider = StreamProvider.family<UserModel, String>((ref, userId) {
  final supabase = Supabase.instance.client;
  final connectivity = ref.watch(connectivityProvider).value;

  if (connectivity == ConnectivityResult.none) {
    // Return a stream that emits a single default user
    return Stream.value(
      UserModel(
        email: " ",
        id: " ",
        name: " ",
      ),
    );
  } else {
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
  }
});

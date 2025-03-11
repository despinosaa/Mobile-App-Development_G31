import 'package:flutter/material.dart';
import 'package:senefavores/state/auth/models/auth_result.dart';

@immutable
class AuthStatus {
  final AuthResult? result;
  final bool isLoading;
  final String? userId;

  const AuthStatus({
    required this.result,
    required this.isLoading,
    required this.userId,
  });

  AuthStatus copyWith({
    AuthResult? result,
    bool? isLoading,
    String? userId,
  }) {
    return AuthStatus(
      result: result ?? this.result,
      isLoading: isLoading ?? this.isLoading,
      userId: userId ?? this.userId,
    );
  }
}

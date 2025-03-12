import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

final snackbarProvider = Provider<SnackbarService>((ref) {
  return SnackbarService();
});

class SnackbarService {
  late BuildContext _context;

  void setContext(BuildContext context) {
    _context = context;
  }

  void showSnackbar(String message, {bool isError = false}) {
    ScaffoldMessenger.of(_context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: isError ? Colors.red : Colors.green,
        behavior: SnackBarBehavior.floating,
      ),
    );
  }
}

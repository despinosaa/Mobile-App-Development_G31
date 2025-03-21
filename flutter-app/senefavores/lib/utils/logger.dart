import 'dart:io';

import 'package:device_info_plus/device_info_plus.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class AppLogger {
  static final SupabaseClient _supabase = Supabase.instance.client;

  // Logs crash info to the `crashes` table
  static Future<void> logCrash({
    required String screen,
    required String crashInfo,
  }) async {
    try {
      await _supabase.from('crashes').insert({
        'screen': screen,
        'crash_info': crashInfo,
      });
    } catch (e) {
      print('Failed to log crash: $e');
    }
  }

  // Logs response time (in milliseconds) to the `response_times` table
  static Future<void> logResponseTime({
    required String screen,
    required int responseTimeMs,
  }) async {
    try {
      final device = await _getDeviceModel();
      await _supabase.from('response_times').insert({
        'screen': screen,
        'response_time': responseTimeMs,
        'device': device,
      });
    } catch (e) {
      print('Failed to log response time: $e');
    }
  }

  // Gets the device model (Android/iOS)
  static Future<String> _getDeviceModel() async {
    final deviceInfo = DeviceInfoPlugin();
    if (Platform.isAndroid) {
      final android = await deviceInfo.androidInfo;
      return '${android.manufacturer} ${android.model}';
    } else if (Platform.isIOS) {
      final ios = await deviceInfo.iosInfo;
      return '${ios.name} ${ios.model}';
    } else {
      return 'Unknown Device';
    }
  }
}

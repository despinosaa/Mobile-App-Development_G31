import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class SessionStorage {
  static const _accessKey = 'accessToken';
  static const _refreshKey = 'refreshToken';
  final _secure = const FlutterSecureStorage();

  Future<void> save({required String access, required String refresh}) =>
      Future.wait([
        _secure.write(key: _accessKey, value: access),
        _secure.write(key: _refreshKey, value: refresh),
      ]);

  Future<String?> readAccess() => _secure.read(key: _accessKey);
  Future<String?> readRefresh() => _secure.read(key: _refreshKey);
  Future<void> clear() => _secure.deleteAll();
}

import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:geolocator/geolocator.dart';

class UserLocationStateNotifier extends StateNotifier<bool> {
  UserLocationStateNotifier() : super(false); // ✅ Initial state: not loading

  /// **Checks if the user is near the target location**
  Future<bool> isUserNearLocation() async {
    try {
      state = true; // ✅ Set loading state

      bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        state = false; // ✅ Stop loading if location services are disabled
        return false;
      }

      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
        if (permission == LocationPermission.denied) {
          state = false;
          return false;
        }
      }

      if (permission == LocationPermission.deniedForever) {
        state = false;
        return false;
      }

      final position = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      );

      // ✅ Coordinates for Cl. 19A #1e-37, Bogotá
      const targetLat = 4.603087;
      const targetLng = -74.065643;

      double distanceInMeters = Geolocator.distanceBetween(
        position.latitude,
        position.longitude,
        targetLat,
        targetLng,
      );

      state = false;
      return distanceInMeters <= 1000;
    } catch (e) {
      state = false;
      return false;
    }
  }
}

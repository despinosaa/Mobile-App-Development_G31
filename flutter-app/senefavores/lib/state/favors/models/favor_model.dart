import 'package:senefavores/core/extension.dart';

class FavorModel {
  final String id;
  final String title;
  final String description;
  final String category;
  final int reward;
  final DateTime? favorTime;
  final DateTime createdAt;
  final String requestUserId;
  final String? acceptUserId;
  final double? latitude;
  final double? longitude;

  FavorModel({
    required this.id,
    required this.title,
    required this.description,
    required this.category,
    required this.reward,
    this.favorTime,
    required this.createdAt,
    required this.requestUserId,
    this.acceptUserId,
    this.latitude,
    this.longitude,
  });

  factory FavorModel.fromJson(Map<String, dynamic> json) {
    return FavorModel(
      id: json['id'] as String,
      title: json['title'] as String,
      description: json['description'] as String,
      category: json['category'] as String,
      reward: json['reward'] as int,
      favorTime: json['favor_time'] != null
          ? DateTime.parse(json['favor_time'])
          : null,
      createdAt: DateTime.parse(json['created_at']),
      requestUserId: json['request_user_id'] as String,
      acceptUserId: json['accept_user_id'] as String?,
      latitude: json['latitude'] != null
          ? (json['latitude'] as num).toDouble()
          : null,
      longitude: json['longitude'] != null
          ? (json['longitude'] as num).toDouble()
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    String category = this.category;
    if (category == 'Tutoria') {
      category = 'Tutoría';
    } else {
      category = category.capitalize();
    }
    return {
      'title': title,
      'description': description,
      'category': category,
      'reward': reward,
      'favor_time': favorTime?.toIso8601String(),
      'created_at': createdAt.toIso8601String(),
      'request_user_id': requestUserId,
      'accept_user_id': acceptUserId,
      'latitude': latitude,
      'longitude': longitude,
    };
  }
}

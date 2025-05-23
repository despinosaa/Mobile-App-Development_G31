class ReviewModel {
  final String id;
  final String title;
  final String description;
  final double stars;
  final DateTime createdAt;
  final String reviewerId;
  final String reviewedId;
  final String favorId;

  ReviewModel({
    required this.id,
    required this.title,
    required this.description,
    required this.stars,
    required this.createdAt,
    required this.reviewerId,
    required this.reviewedId,
    required this.favorId,
  });

  factory ReviewModel.fromJson(Map<String, dynamic> json) {
    return ReviewModel(
      id: json['id'] as String,
      title: json['title'] as String,
      description: json['description'] as String,
      stars: (json['stars'] as num).toDouble(),
      createdAt: DateTime.parse(json['created_at']),
      reviewerId: json['reviewer_id'] as String,
      reviewedId: json['reviewed_id'] as String,
      favorId: json['favor_id'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'title': title,
      'description': description,
      'stars': stars,
      'created_at': createdAt.toIso8601String(),
      'reviewer_id': reviewerId,
      'reviewed_id': reviewedId,
      'favor_id': favorId,
    };
  }
}

class ReviewModel {
  final int id;
  final String title;
  final String description;
  final double stars;
  final DateTime createdAt;
  final int reviewerId;
  final int reviewedId;

  ReviewModel({
    required this.id,
    required this.title,
    required this.description,
    required this.stars,
    required this.createdAt,
    required this.reviewerId,
    required this.reviewedId,
  });

  factory ReviewModel.fromJson(Map<String, dynamic> json) {
    return ReviewModel(
      id: json['id'] as int,
      title: json['title'] as String,
      description: json['description'] as String,
      stars: (json['stars'] as num).toDouble(),
      createdAt: DateTime.parse(json['created_at']),
      reviewerId: json['reviewer_id'] as int,
      reviewedId: json['reviewed_id'] as int,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'description': description,
      'stars': stars,
      'created_at': createdAt.toIso8601String(),
      'reviewer_id': reviewerId,
      'reviewed_id': reviewedId,
    };
  }
}

class UserModel {
  final String id;
  final String? name;
  final String email;
  final String? phone;
  final String? profilePic;
  final double? stars;

  UserModel({
    required this.id,
    this.name,
    required this.email,
    this.phone,
    this.profilePic,
    this.stars,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'] as String,
      name: json['name'] as String?,
      email: json['email'] as String,
      phone: json['phone'] as String?,
      profilePic: json['profile_pic'] as String?,
      stars: (json['stars'] as num?)?.toDouble(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'phone': phone,
      'profile_pic': profilePic,
      'stars': stars,
    };
  }

  /// **✅ Corrected copyWith method**
  UserModel copyWith({
    String? id,
    String? name,
    String? email,
    String? phone,
    String? profilePic,
    double? stars,
  }) {
    return UserModel(
      id: id ?? this.id,
      name: name ?? this.name,
      email: email ?? this.email,
      phone: phone ?? this.phone,
      profilePic: profilePic ?? this.profilePic,
      stars: stars ?? this.stars,
    );
  }
}

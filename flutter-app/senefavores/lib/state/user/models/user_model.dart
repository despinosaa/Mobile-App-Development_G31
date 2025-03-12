class UserModel {
  final int id;
  final String? name;
  final String email;
  final String? phone;
  final String? profilePic;
  final int? stars;

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
      id: json['id'] as int,
      name: json['name'] as String?,
      email: json['email'] as String,
      phone: json['phone'] as String?,
      profilePic: json['profile_pic'] as String?,
      stars: json['stars'] as int?,
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
}

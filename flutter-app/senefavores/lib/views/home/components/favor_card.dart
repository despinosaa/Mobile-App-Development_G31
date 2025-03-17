import 'package:flutter/material.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/core/extension.dart';
import 'package:senefavores/core/format_utils.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/home/models/filter_button_category.dart';
import 'package:senefavores/state/user/models/user_model.dart';
import 'package:senefavores/views/components/build_star_rating.dart';

class FavorCard extends StatelessWidget {
  final FavorModel favor;
  final UserModel user;

  const FavorCard({
    super.key,
    required this.favor,
    required this.user,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      child: Container(
        padding: EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.grey.shade300),
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withOpacity(0.2),
              spreadRadius: 2,
              blurRadius: 1,
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(formatFavorTime(favor.createdAt),
                    style: TextStyle(fontSize: 14, color: Colors.black54)),
                Row(
                  children: [
                    Container(
                      padding:
                          EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: favor.category == FilterButtonCategory.favor.name
                            ? AppColors.lightRed
                            : favor.category == FilterButtonCategory.compra.name
                                ? AppColors.lightSkyBlue
                                : AppColors.orangeWeb,
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Text(
                        favor.category.capitalize(),
                        style: TextStyle(
                            color: Colors.white, fontWeight: FontWeight.bold),
                      ),
                    ),
                    SizedBox(width: 10),
                    Text(
                      formatCurrency(favor.reward),
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ],
            ),
            SizedBox(height: 8),
            Text(favor.title,
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            SizedBox(height: 4),
            Text(
              favor.description,
              style: TextStyle(fontSize: 14, color: Colors.black87),
            ),
            SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    CircleAvatar(
                      radius: 16,
                      backgroundColor: Colors.black,
                      child: user.profilePic != null
                          ? ClipRRect(
                              borderRadius: BorderRadius.circular(50),
                              child: Image.network(
                                user.profilePic!,
                                width: 32,
                                height: 32,
                                fit: BoxFit.cover,
                              ),
                            )
                          : CircleAvatar(
                              radius: 15,
                              backgroundColor: Colors.white,
                              child: Icon(Icons.person, color: Colors.black),
                            ),
                    ),
                    SizedBox(width: 8),
                    Text(
                        user.name != null
                            ? truncateText(user.name!)
                            : 'Anônimo',
                        style: TextStyle(fontWeight: FontWeight.bold)),
                  ],
                ),
                buildStarRating(user.stars ?? 0.0),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

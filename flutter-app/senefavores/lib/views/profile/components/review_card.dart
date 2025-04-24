import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/core/format_utils.dart';
import 'package:senefavores/state/reviews/models/review_model.dart';
import 'package:senefavores/views/components/build_star_rating.dart';

class ReviewCard extends StatelessWidget {
  final ReviewModel review;

  const ReviewCard({super.key, required this.review});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8),
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 6),
        padding: const EdgeInsets.all(12),
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
            /// Row for date + rating stars
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  formatReviewDate(review.createdAt),
                  style: const TextStyle(color: Colors.black54),
                ),
                buildStarRating(
                  review.stars.toDouble(),
                  size: 18,
                  color: AppColors.mikadoYellow,
                )
              ],
            ),
            const SizedBox(height: 6),

            Text(
              review.title,
              style:
                  GoogleFonts.oswald(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 4),

            /// Content
            Text(review.description),
          ],
        ),
      ),
    );
  }
}

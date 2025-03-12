import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:senefavores/core/constant.dart';

class ReviewCard extends StatelessWidget {
  final String date;
  final int rating; // 1 to 5
  final String title;
  final String content;

  const ReviewCard({
    super.key,
    required this.date,
    required this.rating,
    required this.title,
    required this.content,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
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
                date,
                style: const TextStyle(color: Colors.black54),
              ),
              Row(
                children: List.generate(5, (index) {
                  if (index < rating) {
                    return const Icon(Icons.star,
                        color: AppColors.mikadoYellow, size: 18);
                  } else {
                    return const Icon(Icons.star_border,
                        color: AppColors.mikadoYellow, size: 18);
                  }
                }),
              ),
            ],
          ),
          const SizedBox(height: 6),

          /// Title
          Text(
            title,
            style:
                GoogleFonts.oswald(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 4),

          /// Content
          Text(content),
        ],
      ),
    );
  }
}

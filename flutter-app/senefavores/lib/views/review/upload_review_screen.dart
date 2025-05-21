import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/views/components/senefavores_image_and_title_and_profile.dart';
import 'package:flutter_rating_bar/flutter_rating_bar.dart';
import 'package:senefavores/views/home/components/favor_card_display_at.dart';

class UploadReviewScreen extends HookConsumerWidget {
  final String userToReviewId;
  final FavorModel favor;
  final FavorScreen favorScreen;

  UploadReviewScreen(
      {super.key,
      required this.userToReviewId,
      required this.favor,
      required this.favorScreen});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final titleController = useTextEditingController();
    final reviewController = useTextEditingController();
    final rating = useState<double>(0.0);

    void _publishReview() {
      final titleText = titleController.text.trim();
      final reviewText = reviewController.text.trim();

      if (titleText.isEmpty || reviewText.isEmpty || rating.value == 0.0) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
              content: Text('Completa el título, la reseña y la puntuación.')),
        );
        return;
      }

      print('Title: $titleText');
      print('Review: $reviewText');
      print('Rating: ${rating.value}');
    }

    return SafeArea(
      child: Scaffold(
        resizeToAvoidBottomInset: true,
        backgroundColor: Theme.of(context).scaffoldBackgroundColor,
        body: GestureDetector(
          onTap: () => FocusScope.of(context).unfocus(),
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SenefavoresImageAndTitleAndProfile(),
                const SizedBox(height: 20),
                const Text(
                  'Título de tu reseña',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: useTextEditingController(),
                  maxLength: 80,
                  decoration: InputDecoration(
                    filled: true,
                    fillColor: Colors.white,
                    hintText: 'excelente trato...',
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                    enabledBorder: OutlineInputBorder(
                      borderSide:
                          BorderSide(color: Colors.grey.shade400, width: 1.0),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderSide: BorderSide(
                          color: Colors.black,
                          width: 2.0), // ← Your custom focus style
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                ),
                const SizedBox(height: 20),
                const Text(
                  'Tu reseña',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: useTextEditingController(),
                  maxLength: 350,
                  maxLines: 5,
                  decoration: InputDecoration(
                    filled: true,
                    fillColor: Colors.white,
                    hintText: 'Ej: Muy buen trabajo, excelente trato...',
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                    enabledBorder: OutlineInputBorder(
                      borderSide:
                          BorderSide(color: Colors.grey.shade400, width: 1.0),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderSide: BorderSide(
                          color: Colors.black,
                          width: 2.0), // ← Your custom focus style
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                ),
                const SizedBox(height: 20),
                const Text(
                  'Tu puntuación',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                RatingBar.builder(
                  initialRating: 0.0,
                  minRating: 0.0,
                  maxRating: 5.0,
                  allowHalfRating: true,
                  itemCount: 5,
                  itemBuilder: (context, _) => const Icon(
                    Icons.star,
                    color: Colors.amber,
                  ),
                  onRatingUpdate: (rating) {
                    // handle rating
                  },
                ),
                const SizedBox(height: 30),
                SizedBox(
                  width: double.infinity,
                  height: 50,
                  child: ElevatedButton(
                    onPressed: () {
                      _publishReview();
                    },
                    style: ElevatedButton.styleFrom(
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(30),
                      ),
                    ),
                    child: const Text(
                      'Publicar',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: Colors.black,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

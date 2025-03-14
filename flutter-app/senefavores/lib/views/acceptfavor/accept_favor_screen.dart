import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/user/providers/user_provider.dart';
import 'package:senefavores/views/acceptfavor/components/favor_category_chip.dart';
import 'package:senefavores/views/components/build_star_rating.dart';
import 'package:senefavores/views/components/senefavores_image_and_title_and_profile.dart';

class AcceptFavorScreen extends ConsumerWidget {
  final FavorModel favor;
  const AcceptFavorScreen({
    super.key,
    required this.favor,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(userProvider(favor.requestUserId));
    return SafeArea(
      child: Scaffold(
        body: Column(
          mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.max,
          children: [
            SenefavoresImageAndTitleAndProfile(),
            Padding(
              padding: const EdgeInsets.all(10.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.start,
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.max,
                children: [
                  Text(
                    favor.title,
                    style: AppTextStyles.oswaldTitle.copyWith(
                      fontWeight: FontWeight.w400,
                    ),
                  ),
                  SizedBox(height: 10),
                  Text(favor.description, style: AppTextStyles.oswaldBody),
                  SizedBox(height: 10),
                  Text(
                    'Recompensa: ${favor.reward}',
                    style: AppTextStyles.oswaldBody,
                  ),
                  SizedBox(height: 10),
                  Row(
                    children: [
                      Text(
                        'Categoría: ',
                        style: AppTextStyles.oswaldBody,
                      ),
                      FavorCategoryChip(favor: favor.category),
                    ],
                  ),
                  SizedBox(height: 10),
                  Text(
                    'Solicitado por: ',
                    style: AppTextStyles.oswaldSubtitle
                        .copyWith(fontWeight: FontWeight.w300),
                  ),
                  SizedBox(height: 10),
                  user.when(
                    data: (user) {
                      return Row(
                        children: [
                          FaIcon(FontAwesomeIcons.circleUser),
                          SizedBox(width: 10),
                          Text(
                            user.name!,
                            style: AppTextStyles.oswaldBody,
                          ),
                          SizedBox(width: 10),
                          buildStarRating(user.stars!, size: 18),
                        ],
                      );
                    },
                    error: (error, stack) {
                      return Text('Error: $error');
                    },
                    loading: () {
                      return CircularProgressIndicator(
                        color: Colors.black,
                      );
                    },
                  ),
                ],
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: SizedBox(
                width: double.infinity, // ✅ Makes button expand horizontally
                height: 50, // ✅ Adjusts button height
                child: ElevatedButton(
                  onPressed: () {},
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.amber, // ✅ Yellow background
                    foregroundColor: Colors.black, // ✅ Black text
                    shape: RoundedRectangleBorder(
                      borderRadius:
                          BorderRadius.circular(30), // ✅ Fully rounded corners
                    ),
                    elevation: 0, // ✅ No shadow for a flat design
                  ),
                  child: Text("ACEPTAR",
                      style: AppTextStyles.oswaldBody.copyWith(
                        fontWeight: FontWeight.bold,
                      )),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

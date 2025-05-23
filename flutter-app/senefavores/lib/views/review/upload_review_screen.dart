import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hive/hive.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:flutter_rating_bar/flutter_rating_bar.dart';

import 'package:senefavores/core/constant.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';
import 'package:senefavores/state/reviews/providers/upload_review_provider.dart';
import 'package:senefavores/state/user/providers/current_user_provider.dart';
import 'package:senefavores/state/connectivity/connectivity_provider.dart';
import 'package:senefavores/views/components/senefavores_image_and_title_and_profile.dart';

class UploadReviewScreen extends HookConsumerWidget {
  const UploadReviewScreen({
    super.key,
    required this.favor,
    required this.userToReviewId,
    this.favorScreen,
  });

  final FavorModel favor;
  final String userToReviewId;
  final dynamic favorScreen;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final titleCtrl = useTextEditingController();
    final bodyCtrl = useTextEditingController();
    final rating = useState<double>(0);
    final isSending = ref.watch(uploadReviewProvider);
    final connectivity = ref.watch(connectivityProvider).value;
    final hasRestoredDraft = useState(false);

    final titleValue = useValueListenable(titleCtrl);
    final bodyValue = useValueListenable(bodyCtrl);

    // Restore draft when online
    useEffect(() {
      if (!hasRestoredDraft.value && connectivity != ConnectivityResult.none) {
        final box = Hive.box('review_drafts');
        final key = 'draft_review_${favor.id}';

        if (box.containsKey(key)) {
          final cached = box.get(key) as Map;

          titleCtrl.text = cached['title'] ?? '';
          bodyCtrl.text = cached['body'] ?? '';
          rating.value = (cached['rating'] ?? 0).toDouble();

          box.delete(key);
          hasRestoredDraft.value = true;

          SchedulerBinding.instance.addPostFrameCallback((_) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('Se restauró una reseña guardada anteriormente'),
                backgroundColor: Colors.orange,
              ),
            );
          });
        }
      }
      return null;
    }, [connectivity]);

    void snack(String msg, {bool ok = false}) =>
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(msg),
            backgroundColor: ok ? Colors.green : Colors.red,
          ),
        );

    InputDecoration deco({String? hint, String? counter}) => InputDecoration(
          hintText: hint,
          counterText: counter,
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(20),
            borderSide: const BorderSide(color: Colors.grey, width: 4),
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(20),
            borderSide: const BorderSide(color: Colors.black, width: 2),
          ),
          contentPadding:
              const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        );

    Future<void> publish() async {
      final title = titleCtrl.text.trim();
      final body = bodyCtrl.text.trim();

      if (title.isEmpty || body.isEmpty || rating.value == 0) {
        snack('Por favor llena todos los campos');
        return;
      }
      if (title.length > 60) {
        snack('El título no puede exceder 60 caracteres');
        return;
      }
      if (!RegExp(r'[A-Za-z0-9]').hasMatch(title)) {
        snack('El título debe contener al menos un carácter alfanumérico');
        return;
      }
      if (body.length > 250) {
        snack('La reseña no puede exceder 250 caracteres');
        return;
      }

      // Save draft if offline
      if (connectivity == ConnectivityResult.none) {
        final box = Hive.box('review_drafts');
        box.put('draft_review_${favor.id}', {
          'title': title,
          'body': body,
          'rating': rating.value,
        });

        snack(
          'Sin conexión: se guardó la reseña como borrador.',
          ok: false,
        );
        return;
      }

      final currentUser = ref.read(currentUserNotifierProvider);
      if (currentUser == null) {
        snack('Debes iniciar sesión');
        return;
      }

      final ok = await ref.read(uploadReviewProvider.notifier).uploadReview(
            title: title,
            description: body,
            stars: rating.value,
            reviewerId: currentUser.id,
            reviewedId: userToReviewId,
            favorId: favor.id,
          );

      if (!context.mounted) return;
      snack(ok ? '¡Reseña publicada!' : 'Error al publicar la reseña', ok: ok);
      if (ok) Navigator.pop(context);
    }

    return SafeArea(
      child: Scaffold(
        body: Stack(
          children: [
            SingleChildScrollView(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SenefavoresImageAndTitleAndProfile(),
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 8.0),
                    child: Text(
                      'Publicar reseña',
                      style: AppTextStyles.oswaldTitle,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text('Título:', style: AppTextStyles.oswaldSubtitle),
                  const SizedBox(height: 5),
                  TextField(
                    controller: titleCtrl,
                    maxLength: 60,
                    cursorColor: Colors.black,
                    decoration: deco(counter: '${titleValue.text.length}/60'),
                  ),
                  const SizedBox(height: 15),
                  Text('Tu reseña:', style: AppTextStyles.oswaldSubtitle),
                  const SizedBox(height: 5),
                  TextField(
                    controller: bodyCtrl,
                    maxLines: 4,
                    maxLength: 250,
                    cursorColor: Colors.black,
                    decoration: deco(
                      hint: 'Descripción (máx. 250 caracteres)',
                      counter: '${bodyValue.text.length}/250',
                    ),
                  ),
                  const SizedBox(height: 20),
                  Text('Tu puntuación:', style: AppTextStyles.oswaldSubtitle),
                  const SizedBox(height: 8),
                  RatingBar.builder(
                    initialRating: rating.value,
                    minRating: 0.5,
                    allowHalfRating: true,
                    itemCount: 5,
                    unratedColor: Colors.grey.shade300,
                    itemBuilder: (_, __) =>
                        const Icon(Icons.star, color: Colors.amber),
                    onRatingUpdate: (v) => rating.value = v,
                  ),
                  const SizedBox(height: 30),
                ],
              ),
            ),
            Align(
              alignment: Alignment.bottomCenter,
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: SizedBox(
                  width: double.infinity,
                  height: 50,
                  child: ElevatedButton(
                    onPressed: isSending ? null : publish,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.mikadoYellow,
                      foregroundColor: Colors.black,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                        side: const BorderSide(color: Colors.black, width: 1),
                      ),
                      elevation: 0,
                      shadowColor: Colors.transparent,
                    ),
                    child: Text('Publicar', style: AppTextStyles.oswaldTitle),
                  ),
                ),
              ),
            ),
            if (isSending)
              const ColoredBox(
                color: Colors.black38,
                child: Center(child: CircularProgressIndicator()),
              ),
          ],
        ),
      ),
    );
  }
}

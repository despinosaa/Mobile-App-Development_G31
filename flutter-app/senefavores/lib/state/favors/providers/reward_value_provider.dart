import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/favors/providers/category_text_provider.dart';
import 'package:senefavores/state/favors/providers/reward_text_provider.dart';

/// Probability in the range 0–1 (e.g. 0.42 == 42 %)
final acceptanceProbabilityProvider = Provider<double>((ref) {
  final rewardText = ref.watch(rewardTextProvider);
  final categoryText = ref.watch(categoryTextProvider);
  final reward = int.tryParse(rewardText) ?? 0;

  return calculateAcceptanceProbability(categoryText, reward);
});

/// Applies the category-specific formula and clamps the result to [0, 1].
double calculateAcceptanceProbability(String category, int reward) {
  final double priceSquared = reward * reward.toDouble();

  double probability;
  switch (category) {
    case 'Favor':
      probability = priceSquared / 400000000; // reward² / 5 e8
      break;
    case 'Compra':
      probability = priceSquared / 900000000; // reward² / 1 e9
      break;
    case 'Tutoría':
      probability = priceSquared / 7500000000; // reward² / 8 e9
      break;
    default:
      probability = 0; // unknown category
  }

  // Clamp to the valid interval
  if (probability < 0) return 0;
  if (probability > 1) return 1;
  probability = probability * 100;
  return double.parse(probability.toStringAsFixed(2));
}

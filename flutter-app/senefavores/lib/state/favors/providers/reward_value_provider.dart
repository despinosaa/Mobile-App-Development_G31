import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:senefavores/state/favors/providers/category_text_provider.dart';
import 'package:senefavores/state/favors/providers/reward_text_provider.dart';

final acceptanceProbabilityProvider = Provider<int>((ref) {
  final rewardText = ref.watch(rewardTextProvider);
  final categoryText = ref.watch(categoryTextProvider);
  final reward = int.tryParse(rewardText) ?? 0;

  return calculateAcceptanceProbability(categoryText, reward);
});

int calculateAcceptanceProbability(String category, int reward) {
  final maxRewardByCategory = {
    'Favor': 20000,
    'Compra': 30000,
    'Tutoria': 90000,
  };

  final maxReward = maxRewardByCategory[category] ?? 30000; // default fallback
  if (reward <= 0) return 0;
  if (reward >= maxReward) return 100;

  final probability = (reward / maxReward * 100).clamp(0, 100);
  return probability.round();
}

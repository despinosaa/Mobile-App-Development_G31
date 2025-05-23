import 'package:flutter/material.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/core/extension.dart';
import 'package:senefavores/state/favors/models/favor_model.dart';

class FavorStatusChip extends StatelessWidget {
  final FavorModel favor;

  const FavorStatusChip({
    super.key,
    required this.favor,
  });

  @override
  Widget build(BuildContext context) {
    // Determine badge color based on status
    Color statusColor;
    switch (favor.status.toLowerCase()) {
      case 'pending':
        statusColor = Colors.orange;
        break;
      case 'accepted':
        statusColor = Colors.green;
        break;
      case 'done':
      case 'completed':
        statusColor = Colors.grey;
        break;
      case 'cancelled':
        statusColor = Colors.red;
        break;
      default:
        statusColor = AppColors.mikadoYellow;
    }

    // Status badge widget
    final statusBadge = Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: statusColor,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Text(
        favor.status.capitalize(),
        style: const TextStyle(
          color: Colors.white,
          fontWeight: FontWeight.bold,
          fontSize: 12,
        ),
      ),
    );

    return statusBadge;
  }
}

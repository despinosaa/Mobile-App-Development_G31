import 'package:intl/intl.dart';

String formatCurrency(int amount) {
  final formatter = NumberFormat.currency(
    locale: 'es_CO',
    symbol: '\$',
    decimalDigits: 0,
  );
  return formatter.format(amount);
}

String formatFavorTime(DateTime favorTime) {
  return DateFormat.jm().format(favorTime); // 12:00 PM format
}

String truncateText(String text, {int maxLength = 18}) {
  if (text.length > maxLength) {
    return '${text.substring(0, maxLength)}...';
  }
  return text;
}

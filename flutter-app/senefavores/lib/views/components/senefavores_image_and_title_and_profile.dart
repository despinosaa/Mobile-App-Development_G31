import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:senefavores/state/connectivity/connectivity_provider.dart';
import 'package:senefavores/state/snackbar/providers/snackbar_provider.dart';
import 'package:senefavores/views/components/senefavores_image_and_title.dart';
import 'package:senefavores/views/profile/profile_screen.dart';

class SenefavoresImageAndTitleAndProfile extends ConsumerWidget {
  const SenefavoresImageAndTitleAndProfile({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final connectivity = ref.watch(connectivityProvider).value;
    return Row(
      mainAxisSize: MainAxisSize.max,
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        SenefavoresImageAndTitle(),
        IconButton(
          onPressed: () {

            Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => ProfileScreen()),
            );
          },
          icon: FaIcon(FontAwesomeIcons.circleUser),
        ),
      ],
    );
  }
}

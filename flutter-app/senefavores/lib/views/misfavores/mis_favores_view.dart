import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class MisFavoresView extends StatefulWidget {
  const MisFavoresView({super.key});

  @override
  State<MisFavoresView> createState() => _MisFavoresViewState();
}

class _MisFavoresViewState extends State<MisFavoresView> {
  final _future = Supabase.instance.client.from('instruments').select();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: FutureBuilder(
        future: _future,
        builder: (context, snapshot) {
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          final instruments = snapshot.data!;
          return ListView.builder(
            itemCount: instruments.length,
            itemBuilder: ((context, index) {
              final instrument = instruments[index];
              return ListTile(
                title: Text(instrument['name']),
              );
            }),
          );
        },
      ),
    );
  }
}

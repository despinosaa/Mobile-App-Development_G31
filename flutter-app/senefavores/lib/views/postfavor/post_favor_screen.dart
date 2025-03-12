import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';

class PostFavorScreen extends HookWidget {
  const PostFavorScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // Using useTextEditingController for better memory management
    final titleController = useTextEditingController();
    final descriptionController = useTextEditingController();
    final rewardController = useTextEditingController();
    final estimatedTimeController = useTextEditingController();
    final startLocationController = useTextEditingController();
    final endLocationController = useTextEditingController();

    // Selected category state
    final selectedCategory = useState<String?>(null);

    return Scaffold(
      appBar: AppBar(title: Text("Crear Favor")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Title Field
            Text("Título:", style: TextStyle(fontWeight: FontWeight.bold)),
            TextField(controller: titleController),

            SizedBox(height: 10),

            // Description Field
            Text("Descripción:", style: TextStyle(fontWeight: FontWeight.bold)),
            TextField(
              controller: descriptionController,
              maxLines: 4,
              decoration: InputDecoration(
                border: OutlineInputBorder(),
              ),
            ),

            SizedBox(height: 10),

            // Reward Field
            Text("Recompensa:", style: TextStyle(fontWeight: FontWeight.bold)),
            TextField(
              controller: rewardController,
              keyboardType: TextInputType.number,
            ),

            SizedBox(height: 10),

            // Category Selection
            Text("Categoría:", style: TextStyle(fontWeight: FontWeight.bold)),
            Row(
              mainAxisAlignment: MainAxisAlignment.start,
              children: [
                _categoryButton("Favor", Colors.redAccent, selectedCategory),
                SizedBox(width: 10),
                _categoryButton("Compra", Colors.blueAccent, selectedCategory),
                SizedBox(width: 10),
                _categoryButton(
                    "Tutoría", Colors.orangeAccent, selectedCategory),
              ],
            ),

            SizedBox(height: 10),

            // Expandable Section - Otros
            ExpansionTile(
              title:
                  Text("Otros:", style: TextStyle(fontWeight: FontWeight.bold)),
              children: [
                _textInputField("Tiempo estimado:", estimatedTimeController),
                _textInputField("Lugar inicio:", startLocationController),
                _textInputField("Lugar final:", endLocationController),
              ],
            ),
          ],
        ),
      ),
    );
  }

  // Widget for category selection buttons
  Widget _categoryButton(
      String text, Color color, ValueNotifier<String?> selectedCategory) {
    final isSelected = selectedCategory.value == text;
    return GestureDetector(
      onTap: () => selectedCategory.value = text,
      child: Chip(
        label: Text(text),
        backgroundColor: isSelected ? color.withOpacity(0.8) : Colors.grey[300],
        labelStyle: TextStyle(
          color: isSelected ? Colors.white : Colors.black,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }

  // Widget for "Otros" section text fields
  Widget _textInputField(String label, TextEditingController controller) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 5),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: TextStyle(fontWeight: FontWeight.bold)),
          TextField(controller: controller),
        ],
      ),
    );
  }
}

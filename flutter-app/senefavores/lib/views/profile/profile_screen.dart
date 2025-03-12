import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:senefavores/core/constant.dart';
import 'package:senefavores/views/profile/components/review_card.dart';
import 'package:senefavores/views/navigation/navigation_screen.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // Hardcoded list of reviews
    final List<Map<String, dynamic>> reviews = [
      {
        "date": "21/11/2024",
        "rating": 5,
        "title": "Compra 1",
        "content": "Excelente servicio, todo salió perfecto.",
      },
      {
        "date": "05/01/2025",
        "rating": 4,
        "title": "Tutoria 2",
        "content": "Muy buena explicación y apoyo, recomendaría.",
      },
      {
        "date": "12/02/2025",
        "rating": 3,
        "title": "Favor 3",
        "content":
            "El servicio estuvo bien, pero hay aspectos a mejorar mucho mucho.",
      },
      {
        "date": "28/02/2025",
        "rating": 2,
        "title": "Compra 4",
        "content": "No cumplió todas mis expectativas.",
      },
      {
        "date": "10/03/2025",
        "rating": 5,
        "title": "Tutoria 5",
        "content": "¡Excelente tutoría! Aprendí mucho.",
      },
      {
        "date": "18/03/2025",
        "rating": 4,
        "title": "Favor 6",
        "content": "Muy bueno, lo recomiendo.",
      },
      {
        "date": "25/03/2025",
        "rating": 1,
        "title": "Compra 7",
        "content": "La experiencia fue muy insatisfactoria.",
      },
    ];

    return Scaffold(
      // Remove the default AppBar; use a custom header in the body
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            children: [
              // Custom header (logo and title, no back arrow)
              Row(
                mainAxisAlignment: MainAxisAlignment.start,
                children: [
                  Image.asset(
                    'assets/images/senefavores_logo.png',
                    height: 50,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    "SeneFavores",
                    style:
                        GoogleFonts.oswald(fontSize: 24, color: Colors.black),
                  ),
                ],
              ),
              const SizedBox(height: 20),
              // Profile Information Card
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.grey.shade300),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.grey.withOpacity(0.2),
                      spreadRadius: 2,
                      blurRadius: 1,
                    ),
                  ],
                ),
                child: Row(
                  children: [
                    CircleAvatar(
                      radius: 32,
                      backgroundColor: Colors.grey.shade300,
                      child: const Icon(Icons.person,
                          size: 40, color: Colors.black),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            "Nombre Usuario",
                            style: GoogleFonts.oswald(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          Row(
                            children: List.generate(5, (index) {
                              return const Icon(Icons.star,
                                  color: AppColors.mikadoYellow, size: 18);
                            }),
                          ),
                          const SizedBox(height: 4),
                          const Text(
                            "correo@uniandes.edu.co",
                            style: TextStyle(color: Colors.black54),
                          ),
                          const Text(
                            "+57 300 1234567",
                            style: TextStyle(color: Colors.black54),
                          ),
                        ],
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.edit),
                      onPressed: () {
                        // TODO: Implement edit profile functionality
                      },
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),
              // Reviews Section Title
              Align(
                alignment: Alignment.centerLeft,
                child: Text(
                  "Mis reseñas:",
                  style: GoogleFonts.oswald(
                      fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ),
              const SizedBox(height: 10),
              // Scrollable list of Review Cards
              Expanded(
                child: ListView.builder(
                  itemCount: reviews.length,
                  itemBuilder: (context, index) {
                    final review = reviews[index];
                    return ReviewCard(
                      date: review["date"],
                      rating: review["rating"],
                      title: review["title"],
                      content: review["content"],
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
      // Bottom Navigation Bar (Same as other screens)
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: 0, // Default index (adjust if needed)
        onTap: (index) {
          Navigator.pushReplacement(
            context,
            MaterialPageRoute(
              builder: (_) => NavigationScreen(initialIndex: index),
            ),
          );
        },
        items: const [
          BottomNavigationBarItem(
            icon: FaIcon(FontAwesomeIcons.igloo),
            label: 'Home',
          ),
          BottomNavigationBarItem(
            icon: FaIcon(FontAwesomeIcons.circlePlus),
            label: 'Pedir favor',
          ),
          BottomNavigationBarItem(
            icon: FaIcon(FontAwesomeIcons.clockRotateLeft),
            label: 'Mis favores',
          ),
        ],
        type: BottomNavigationBarType.fixed,
        elevation: 0,
        selectedItemColor: Colors.black,
        selectedFontSize: 12,
      ),
    );
  }
}

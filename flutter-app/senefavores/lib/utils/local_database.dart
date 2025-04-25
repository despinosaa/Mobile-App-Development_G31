import 'dart:io';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'package:sqflite/sqflite.dart';
import 'package:senefavores/state/user/models/user_model.dart';
import 'package:senefavores/state/reviews/models/review_model.dart';

class LocalDatabase {
  static final LocalDatabase instance = LocalDatabase._init();
  static Database? _db;
  LocalDatabase._init();

  Future<Database> get database async {
    if (_db != null) return _db!;
    _db = await _initDB('senefavores.db');
    return _db!;
  }

  Future<Database> _initDB(String fileName) async {
    final dir = await getApplicationDocumentsDirectory();
    final path = join(dir.path, fileName);
    return await openDatabase(path, version: 1, onCreate: _createTables);
  }

  Future _createTables(Database db, int version) async {
    await db.execute('''
      CREATE TABLE clients (
        id TEXT PRIMARY KEY,
        name TEXT,
        email TEXT NOT NULL,
        phone TEXT,
        profile_pic TEXT,
        stars REAL
      )
    ''');
    await db.execute('''
      CREATE TABLE reviews (
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        stars REAL NOT NULL,
        created_at TEXT NOT NULL,
        reviewer_id TEXT NOT NULL,
        reviewed_id TEXT NOT NULL
      )
    ''');
  }

  // --- Client (UserModel) CRUD ---
  Future<UserModel?> getCachedUser(String id) async {
    final db = await database;
    final maps = await db.query(
      'clients',
      columns: ['id','name','email','phone','profile_pic','stars'],
      where: 'id = ?',
      whereArgs: [id],
    );
    if (maps.isNotEmpty) return UserModel.fromJson(maps.first);
    return null;
  }

  Future<void> cacheUser(UserModel u) async {
    final db = await database;
    await db.insert(
      'clients',
      u.toJson(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  // --- ReviewModel CRUD ---
  Future<List<ReviewModel>> getCachedReviews(String userId) async {
    final db = await database;
    final maps = await db.query(
      'reviews',
      where: 'reviewed_id = ?',
      whereArgs: [userId],
      orderBy: 'created_at DESC',
    );
    return maps.map((m) => ReviewModel.fromJson(m)).toList();
  }

  Future<void> clearCachedReviews(String userId) async {
    final db = await database;
    await db.delete(
      'reviews',
      where: 'reviewed_id = ?',
      whereArgs: [userId],
    );
  }

  Future<void> cacheReviews(List<ReviewModel> reviews) async {
    final db = await database;
    final batch = db.batch();
    for (final r in reviews) {
      batch.insert(
        'reviews',
        r.toJson(),
        conflictAlgorithm: ConflictAlgorithm.replace,
      );
    }
    await batch.commit(noResult: true);
  }
}

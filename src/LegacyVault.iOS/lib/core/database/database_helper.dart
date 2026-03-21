import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

import '../models/category_model.dart';
import '../models/page_model.dart';

class DatabaseHelper {
  DatabaseHelper._();
  static final DatabaseHelper instance = DatabaseHelper._();

  static Database? _database;

  Future<Database> get database async {
    _database ??= await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, 'legacy_vault.db');
    return openDatabase(
      path,
      version: 1,
      onCreate: _onCreate,
      onConfigure: (db) async {
        await db.execute('PRAGMA foreign_keys = ON');
      },
    );
  }

  Future<void> _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE categories (
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        icon TEXT NOT NULL,
        type TEXT NOT NULL,
        is_encrypted INTEGER NOT NULL DEFAULT 0,
        encryption_salt TEXT,
        password_hint TEXT,
        is_favorite INTEGER NOT NULL DEFAULT 0,
        page_count INTEGER NOT NULL DEFAULT 0,
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL
      )
    ''');

    await db.execute('''
      CREATE TABLE pages (
        id TEXT PRIMARY KEY,
        category_id TEXT NOT NULL,
        type TEXT NOT NULL,
        title TEXT NOT NULL,
        content TEXT NOT NULL,
        is_encrypted INTEGER NOT NULL DEFAULT 0,
        encryption_iv TEXT,
        is_favorite INTEGER NOT NULL DEFAULT 0,
        sort_order INTEGER NOT NULL DEFAULT 0,
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL,
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
      )
    ''');

    await db.execute('CREATE INDEX idx_pages_category ON pages(category_id)');
    await db.execute('CREATE INDEX idx_pages_type ON pages(type)');
  }

  // ─── Category DAO ────────────────────────────────────────────────────────────

  Future<List<Category>> getAllCategories() async {
    final db = await database;
    final maps = await db.query(
      'categories',
      orderBy: 'is_favorite DESC, name ASC',
    );
    return maps.map(Category.fromMap).toList();
  }

  Future<Category?> getCategoryById(String id) async {
    final db = await database;
    final maps = await db.query(
      'categories',
      where: 'id = ?',
      whereArgs: [id],
    );
    if (maps.isEmpty) return null;
    return Category.fromMap(maps.first);
  }

  Future<void> insertCategory(Category category) async {
    final db = await database;
    await db.insert(
      'categories',
      category.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<void> updateCategory(Category category) async {
    final db = await database;
    await db.update(
      'categories',
      category.toMap(),
      where: 'id = ?',
      whereArgs: [category.id],
    );
  }

  Future<void> deleteCategory(String id) async {
    final db = await database;
    await db.delete('categories', where: 'id = ?', whereArgs: [id]);
  }

  Future<void> updateCategoryPageCount(String categoryId) async {
    final db = await database;
    final result = await db.rawQuery(
      'SELECT COUNT(*) as cnt FROM pages WHERE category_id = ?',
      [categoryId],
    );
    final count = result.first['cnt'] as int? ?? 0;
    await db.update(
      'categories',
      {
        'page_count': count,
        'updated_at': DateTime.now().toIso8601String(),
      },
      where: 'id = ?',
      whereArgs: [categoryId],
    );
  }

  // ─── Page DAO ────────────────────────────────────────────────────────────────

  Future<List<PageModel>> getPagesByCategory(String categoryId) async {
    final db = await database;
    final maps = await db.query(
      'pages',
      where: 'category_id = ?',
      whereArgs: [categoryId],
      orderBy: 'sort_order ASC, updated_at DESC',
    );
    return maps.map(PageModel.fromMap).toList();
  }

  Future<PageModel?> getPageById(String id) async {
    final db = await database;
    final maps = await db.query(
      'pages',
      where: 'id = ?',
      whereArgs: [id],
    );
    if (maps.isEmpty) return null;
    return PageModel.fromMap(maps.first);
  }

  Future<List<PageModel>> getPagesByType(String type) async {
    final db = await database;
    final maps = await db.query(
      'pages',
      where: 'type = ?',
      whereArgs: [type],
      orderBy: 'updated_at DESC',
    );
    return maps.map(PageModel.fromMap).toList();
  }

  Future<List<PageModel>> searchPages(String query) async {
    final db = await database;
    final maps = await db.query(
      'pages',
      where: 'title LIKE ?',
      whereArgs: ['%$query%'],
      orderBy: 'updated_at DESC',
    );
    return maps.map(PageModel.fromMap).toList();
  }

  Future<void> insertPage(PageModel page) async {
    final db = await database;
    await db.insert(
      'pages',
      page.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
    await updateCategoryPageCount(page.categoryId);
  }

  Future<void> updatePage(PageModel page) async {
    final db = await database;
    await db.update(
      'pages',
      page.toMap(),
      where: 'id = ?',
      whereArgs: [page.id],
    );
  }

  Future<void> deletePage(String id, String categoryId) async {
    final db = await database;
    await db.delete('pages', where: 'id = ?', whereArgs: [id]);
    await updateCategoryPageCount(categoryId);
  }

  Future<List<PageModel>> getAllPages() async {
    final db = await database;
    final maps = await db.query('pages', orderBy: 'updated_at DESC');
    return maps.map(PageModel.fromMap).toList();
  }

  // ─── Bulk Import (for Backup Restore) ────────────────────────────────────────

  Future<void> importBackup({
    required List<Category> categories,
    required List<PageModel> pages,
  }) async {
    final db = await database;
    await db.transaction((txn) async {
      for (final cat in categories) {
        await txn.insert(
          'categories',
          cat.toMap(),
          conflictAlgorithm: ConflictAlgorithm.replace,
        );
      }
      for (final page in pages) {
        await txn.insert(
          'pages',
          page.toMap(),
          conflictAlgorithm: ConflictAlgorithm.replace,
        );
      }
    });
  }
}

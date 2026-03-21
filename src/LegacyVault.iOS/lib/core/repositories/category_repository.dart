import 'package:uuid/uuid.dart';

import '../database/database_helper.dart';
import '../models/category_model.dart';

class CategoryRepository {
  final DatabaseHelper _db;
  final Uuid _uuid;

  CategoryRepository({
    DatabaseHelper? db,
    Uuid? uuid,
  })  : _db = db ?? DatabaseHelper.instance,
        _uuid = uuid ?? const Uuid();

  Future<List<Category>> getAllCategories() => _db.getAllCategories();

  Future<Category?> getCategoryById(String id) => _db.getCategoryById(id);

  Future<Category> createCategory({
    required String name,
    required String icon,
    required String type,
    bool isEncrypted = false,
    String? encryptionSalt,
    String? passwordHint,
  }) async {
    final now = DateTime.now().toIso8601String();
    final category = Category(
      id: _uuid.v4(),
      name: name,
      icon: icon,
      type: type,
      isEncrypted: isEncrypted,
      encryptionSalt: encryptionSalt,
      passwordHint: passwordHint,
      isFavorite: false,
      pageCount: 0,
      createdAt: now,
      updatedAt: now,
    );
    await _db.insertCategory(category);
    return category;
  }

  Future<Category> updateCategory(Category category) async {
    final updated = category.copyWith(
      updatedAt: DateTime.now().toIso8601String(),
    );
    await _db.updateCategory(updated);
    return updated;
  }

  Future<void> toggleFavorite(Category category) async {
    await _db.updateCategory(
      category.copyWith(
        isFavorite: !category.isFavorite,
        updatedAt: DateTime.now().toIso8601String(),
      ),
    );
  }

  Future<void> deleteCategory(String id) => _db.deleteCategory(id);
}

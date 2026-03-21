import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/models/category_model.dart';
import '../../core/repositories/category_repository.dart';

class CategoriesState {
  final List<Category> categories;
  final bool isLoading;
  final String? error;

  const CategoriesState({
    this.categories = const [],
    this.isLoading = false,
    this.error,
  });

  CategoriesState copyWith({
    List<Category>? categories,
    bool? isLoading,
    String? error,
  }) {
    return CategoriesState(
      categories: categories ?? this.categories,
      isLoading: isLoading ?? this.isLoading,
      error: error,
    );
  }
}

class CategoriesNotifier extends StateNotifier<CategoriesState> {
  final CategoryRepository _repo;

  CategoriesNotifier(this._repo) : super(const CategoriesState()) {
    loadCategoriesAsync();
  }

  Future<void> loadCategoriesAsync() async {
    state = state.copyWith(isLoading: true);
    try {
      final cats = await _repo.getAllCategories();
      state = CategoriesState(categories: cats);
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<Category?> createCategoryAsync({
    required String name,
    required String icon,
    bool isEncrypted = false,
    String? encryptionSalt,
    String? passwordHint,
  }) async {
    try {
      final cat = await _repo.createCategory(
        name: name,
        icon: icon,
        type: 'GENERAL',
        isEncrypted: isEncrypted,
        encryptionSalt: encryptionSalt,
        passwordHint: passwordHint,
      );
      await loadCategoriesAsync();
      return cat;
    } catch (e) {
      state = state.copyWith(error: e.toString());
      return null;
    }
  }

  Future<void> updateCategoryAsync(Category category) async {
    try {
      await _repo.updateCategory(category);
      await loadCategoriesAsync();
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> toggleFavoriteAsync(Category category) async {
    try {
      await _repo.toggleFavorite(category);
      await loadCategoriesAsync();
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> deleteCategoryAsync(String id) async {
    try {
      await _repo.deleteCategory(id);
      await loadCategoriesAsync();
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }
}

final categoryRepositoryProvider = Provider<CategoryRepository>(
  (_) => CategoryRepository(),
);

final categoriesNotifierProvider =
    StateNotifierProvider<CategoriesNotifier, CategoriesState>((ref) {
  return CategoriesNotifier(ref.watch(categoryRepositoryProvider));
});

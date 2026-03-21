import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/models/category_model.dart';
import '../../core/models/page_model.dart';
import '../../core/repositories/category_repository.dart';
import '../../core/repositories/page_repository.dart';
import '../categories/categories_notifier.dart';

class PageListState {
  final List<PageModel> pages;
  final Category? category;
  final bool isLoading;
  final String? error;

  const PageListState({
    this.pages = const [],
    this.category,
    this.isLoading = false,
    this.error,
  });

  PageListState copyWith({
    List<PageModel>? pages,
    Category? category,
    bool? isLoading,
    String? error,
  }) =>
      PageListState(
        pages: pages ?? this.pages,
        category: category ?? this.category,
        isLoading: isLoading ?? this.isLoading,
        error: error,
      );
}

class PageListNotifier extends StateNotifier<PageListState> {
  final PageRepository _pageRepo;
  final CategoryRepository _categoryRepo;
  final String categoryId;

  PageListNotifier({
    required this.categoryId,
    required PageRepository pageRepo,
    required CategoryRepository categoryRepo,
  })  : _pageRepo = pageRepo,
        _categoryRepo = categoryRepo,
        super(const PageListState()) {
    loadPages();
  }

  Future<void> loadPages() async {
    state = state.copyWith(isLoading: true);
    try {
      final category = await _categoryRepo.getCategoryById(categoryId);
      final pages = await _pageRepo.getPagesForCategory(categoryId);
      state = PageListState(
        pages: pages,
        category: category,
        isLoading: false,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> deletePage(String pageId) async {
    try {
      await _pageRepo.deletePage(pageId, categoryId);
      await loadPages();
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> toggleFavorite(PageModel page) async {
    try {
      await _pageRepo.toggleFavorite(page);
      await loadPages();
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }
}

final pageRepositoryProvider = Provider<PageRepository>(
  (_) => PageRepository(),
);

final pageListNotifierProvider = StateNotifierProvider.autoDispose
    .family<PageListNotifier, PageListState, String>(
  (ref, categoryId) => PageListNotifier(
    categoryId: categoryId,
    pageRepo: ref.watch(pageRepositoryProvider),
    categoryRepo: ref.watch(categoryRepositoryProvider),
  ),
);

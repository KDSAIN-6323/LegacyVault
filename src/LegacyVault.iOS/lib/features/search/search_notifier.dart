import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/models/page_model.dart';
import '../../core/repositories/page_repository.dart';
import '../pages/page_list_notifier.dart';

class SearchState {
  final List<PageModel> results;
  final String query;
  final bool isLoading;

  const SearchState({
    this.results = const [],
    this.query = '',
    this.isLoading = false,
  });

  SearchState copyWith({
    List<PageModel>? results,
    String? query,
    bool? isLoading,
  }) =>
      SearchState(
        results: results ?? this.results,
        query: query ?? this.query,
        isLoading: isLoading ?? this.isLoading,
      );
}

class SearchNotifier extends StateNotifier<SearchState> {
  final PageRepository _repo;

  SearchNotifier(this._repo) : super(const SearchState());

  Future<void> searchAsync(String query) async {
    if (query.trim().isEmpty) {
      state = SearchState(query: query, isLoading: false);
      return;
    }
    state = state.copyWith(query: query, isLoading: true);
    try {
      final results = await _repo.searchPagesAsync(query.trim());
      state = SearchState(results: results, query: query);
    } catch (e) {
      state = state.copyWith(isLoading: false);
    }
  }

  void clear() {
    state = const SearchState();
  }
}

final searchNotifierProvider =
    StateNotifierProvider<SearchNotifier, SearchState>((ref) {
  return SearchNotifier(ref.watch(pageRepositoryProvider));
});

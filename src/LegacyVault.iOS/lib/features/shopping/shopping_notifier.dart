import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/models/page_content.dart';
import '../../core/models/page_model.dart';
import '../../core/models/page_type.dart';
import '../../core/repositories/page_repository.dart';
import '../pages/page_list_notifier.dart';

class ShoppingState {
  final List<({PageModel page, ShoppingListContent content})> lists;
  final bool isLoading;
  final String? error;

  const ShoppingState({
    this.lists = const [],
    this.isLoading = false,
    this.error,
  });

  ShoppingState copyWith({
    List<({PageModel page, ShoppingListContent content})>? lists,
    bool? isLoading,
    String? error,
  }) =>
      ShoppingState(
        lists: lists ?? this.lists,
        isLoading: isLoading ?? this.isLoading,
        error: error,
      );
}

class ShoppingNotifier extends StateNotifier<ShoppingState> {
  final PageRepository _repo;

  ShoppingNotifier(this._repo) : super(const ShoppingState()) {
    loadShoppingLists();
  }

  Future<void> loadShoppingLists() async {
    state = state.copyWith(isLoading: true);
    try {
      final pages = await _repo.getPagesByType(PageType.shoppingList);
      final lists = <({PageModel page, ShoppingListContent content})>[];

      for (final page in pages) {
        final content = _repo.decryptAndParseContent(page);
        if (content is ShoppingListContent) {
          lists.add((page: page, content: content));
        } else if (!page.isEncrypted) {
          try {
            final c = PageContent.fromJson(page.type.dbValue, page.content);
            if (c is ShoppingListContent) {
              lists.add((page: page, content: c));
            }
          } catch (_) {}
        }
      }

      state = ShoppingState(lists: lists);
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> toggleItem(PageModel page, ShoppingListContent content, int itemIndex) async {
    final items = List.of(content.items);
    items[itemIndex] = items[itemIndex].copyWith(
      checked: !items[itemIndex].checked,
    );
    final updated = content.copyWith(items: items);

    try {
      await _repo.updatePage(
        page: page,
        title: page.title,
        content: updated,
      );
      await loadShoppingLists();
    } catch (_) {}
  }
}

final shoppingNotifierProvider =
    StateNotifierProvider<ShoppingNotifier, ShoppingState>((ref) {
  return ShoppingNotifier(ref.watch(pageRepositoryProvider));
});

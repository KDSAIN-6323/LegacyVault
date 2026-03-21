import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/models/page_content.dart';
import '../../core/models/page_model.dart';
import '../../core/models/page_type.dart';
import '../../core/repositories/page_repository.dart';
import '../pages/page_list_notifier.dart';

class PageDetailState {
  final PageModel? page;
  final PageContent? content;
  final bool isLoading;
  final bool isSaving;
  final String? error;

  const PageDetailState({
    this.page,
    this.content,
    this.isLoading = false,
    this.isSaving = false,
    this.error,
  });

  PageDetailState copyWith({
    PageModel? page,
    PageContent? content,
    bool? isLoading,
    bool? isSaving,
    String? error,
  }) =>
      PageDetailState(
        page: page ?? this.page,
        content: content ?? this.content,
        isLoading: isLoading ?? this.isLoading,
        isSaving: isSaving ?? this.isSaving,
        error: error,
      );
}

class PageDetailNotifier extends StateNotifier<PageDetailState> {
  final PageRepository _repo;
  final String categoryId;
  final String? pageId;
  final PageType? initialType;

  PageDetailNotifier({
    required this.categoryId,
    required this.pageId,
    required this.initialType,
    required PageRepository repo,
  })  : _repo = repo,
        super(const PageDetailState()) {
    if (pageId != null) {
      _loadPage();
    }
  }

  Future<void> _loadPage() async {
    state = state.copyWith(isLoading: true);
    try {
      final page = await _repo.getPageById(pageId!);
      if (page == null) {
        state = state.copyWith(isLoading: false, error: 'Page not found');
        return;
      }
      final content = _repo.decryptAndParseContent(page);
      state = PageDetailState(page: page, content: content);
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<bool> savePage({
    required String title,
    required PageContent content,
  }) async {
    state = state.copyWith(isSaving: true);
    try {
      if (state.page == null) {
        // Create new
        final type = initialType ?? PageType.note;
        await _repo.createPage(
          categoryId: categoryId,
          type: type,
          title: title,
          content: content,
          encryptForVault: true,
        );
      } else {
        // Update existing
        await _repo.updatePage(
          page: state.page!,
          title: title,
          content: content,
        );
      }
      state = state.copyWith(isSaving: false);
      return true;
    } catch (e) {
      state = state.copyWith(isSaving: false, error: e.toString());
      return false;
    }
  }

  PageType get effectiveType {
    if (state.page != null) return state.page!.type;
    return initialType ?? PageType.note;
  }
}

final pageDetailNotifierProvider = StateNotifierProvider.autoDispose.family<
    PageDetailNotifier,
    PageDetailState,
    ({String categoryId, String? pageId, String? pageType})>(
  (ref, args) => PageDetailNotifier(
    categoryId: args.categoryId,
    pageId: args.pageId,
    initialType: args.pageType != null
        ? PageTypeExtension.fromDbValue(args.pageType!)
        : null,
    repo: ref.watch(pageRepositoryProvider),
  ),
);

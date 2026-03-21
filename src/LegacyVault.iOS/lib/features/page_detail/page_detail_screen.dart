import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/models/page_content.dart';
import '../../core/models/page_type.dart';
import '../../theme/app_colors.dart';
import '../pages/editors/home_inventory_editor.dart';
import '../pages/editors/note_editor.dart';
import '../pages/editors/password_editor.dart';
import '../pages/editors/quote_editor.dart';
import '../pages/editors/recipe_editor.dart';
import '../pages/editors/reminder_editor.dart';
import '../pages/editors/shopping_list_editor.dart';
import 'page_detail_notifier.dart';

class PageDetailScreen extends ConsumerStatefulWidget {
  final String categoryId;
  final String? pageId;
  final String? pageType;

  const PageDetailScreen({
    super.key,
    required this.categoryId,
    required this.pageId,
    required this.pageType,
  });

  @override
  ConsumerState<PageDetailScreen> createState() => _PageDetailScreenState();
}

class _PageDetailScreenState extends ConsumerState<PageDetailScreen> {
  final _titleCtrl = TextEditingController();
  PageContent? _currentContent;
  bool _initialized = false;

  @override
  void dispose() {
    _titleCtrl.dispose();
    super.dispose();
  }

  void _initContent(PageDetailState state) {
    if (_initialized) return;
    _initialized = true;
    if (state.page != null) {
      _titleCtrl.text = state.page!.title;
      _currentContent = state.content;
    }
  }

  Future<void> _save() async {
    final notifierArgs = (
      categoryId: widget.categoryId,
      pageId: widget.pageId,
      pageType: widget.pageType,
    );
    final notifier = ref.read(pageDetailNotifierProvider(notifierArgs).notifier);

    final title = _titleCtrl.text.trim();
    if (title.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Title is required')),
      );
      return;
    }

    if (_currentContent == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please fill in the page content')),
      );
      return;
    }

    final success = await notifier.savePage(
      title: title,
      content: _currentContent!,
    );

    if (success && mounted) {
      context.pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final notifierArgs = (
      categoryId: widget.categoryId,
      pageId: widget.pageId,
      pageType: widget.pageType,
    );
    final state = ref.watch(pageDetailNotifierProvider(notifierArgs));
    final notifier =
        ref.read(pageDetailNotifierProvider(notifierArgs).notifier);

    if (state.isLoading) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    _initContent(state);
    final pageType = notifier.effectiveType;
    final isNew = widget.pageId == null;

    return Scaffold(
      appBar: AppBar(
        title: Text(
          isNew ? 'New ${pageType.label}' : 'Edit ${pageType.label}',
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
        leading: IconButton(
          icon: const Icon(Icons.close),
          onPressed: () => context.pop(),
        ),
        actions: [
          if (state.isSaving)
            const Padding(
              padding: EdgeInsets.all(16),
              child: SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(strokeWidth: 2),
              ),
            )
          else
            TextButton(
              onPressed: _save,
              child: const Text(
                'Save',
                style: TextStyle(
                  color: AppColors.accentLight,
                  fontWeight: FontWeight.bold,
                  fontSize: 16,
                ),
              ),
            ),
        ],
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Type badge
              Row(
                children: [
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.accent.withAlpha(40),
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(color: AppColors.accent.withAlpha(80)),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(pageType.emoji,
                            style: const TextStyle(fontSize: 14)),
                        const SizedBox(width: 6),
                        Text(
                          pageType.label,
                          style: const TextStyle(
                            color: AppColors.accentLight,
                            fontSize: 13,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              // Title field
              TextField(
                controller: _titleCtrl,
                decoration: const InputDecoration(
                  labelText: 'Title',
                  prefixIcon: Icon(Icons.title),
                ),
                textCapitalization: TextCapitalization.words,
              ),
              const SizedBox(height: 20),
              // Content editor
              _buildEditor(pageType, state),
              if (state.error != null) ...[
                const SizedBox(height: 12),
                Text(state.error!,
                    style: const TextStyle(color: AppColors.error)),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildEditor(PageType type, PageDetailState state) {
    final initialContent = _currentContent;
    switch (type) {
      case PageType.note:
        return NoteEditor(
          initial: initialContent is NoteContent ? initialContent : null,
          onChanged: (c) => _currentContent = c,
        );
      case PageType.password:
        return PasswordEditor(
          initial: initialContent is PasswordContent ? initialContent : null,
          onChanged: (c) => _currentContent = c,
        );
      case PageType.recipe:
        return RecipeEditor(
          categoryId: widget.categoryId,
          initial: initialContent is RecipeContent ? initialContent : null,
          onChanged: (c) => _currentContent = c,
        );
      case PageType.quote:
        return QuoteEditor(
          initial: initialContent is QuoteContent ? initialContent : null,
          onChanged: (c) => _currentContent = c,
        );
      case PageType.homeInventory:
        return HomeInventoryEditor(
          initial:
              initialContent is HomeInventoryContent ? initialContent : null,
          onChanged: (c) => _currentContent = c,
        );
      case PageType.reminder:
        return ReminderEditor(
          initial: initialContent is ReminderContent ? initialContent : null,
          onChanged: (c) => _currentContent = c,
        );
      case PageType.shoppingList:
        return ShoppingListEditor(
          initial: initialContent is ShoppingListContent ? initialContent : null,
          onChanged: (c) => _currentContent = c,
        );
    }
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/models/page_model.dart';
import '../../core/models/page_type.dart';
import '../../shared/widgets/page_card.dart';
import '../../theme/app_colors.dart';
import 'page_list_notifier.dart';

class PageListScreen extends ConsumerWidget {
  final String categoryId;

  const PageListScreen({super.key, required this.categoryId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(pageListNotifierProvider(categoryId));
    final category = state.category;

    return Scaffold(
      appBar: AppBar(
        title: Text(category?.name ?? 'Pages',
            style: const TextStyle(fontWeight: FontWeight.bold)),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
        actions: [
          if (category != null && category.isEncrypted)
            const Padding(
              padding: EdgeInsets.only(right: 16),
              child: Icon(Icons.lock, color: AppColors.accentLight, size: 20),
            ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showPageTypePicker(context),
        child: const Icon(Icons.add),
      ),
      body: state.isLoading
          ? const Center(child: CircularProgressIndicator())
          : state.pages.isEmpty
              ? _EmptyState(onAdd: () => _showPageTypePicker(context))
              : _PageList(categoryId: categoryId, pages: state.pages),
    );
  }

  void _showPageTypePicker(BuildContext context) {
    showModalBottomSheet(
      context: context,
      backgroundColor: AppColors.darkSurface,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (_) => _PageTypePicker(categoryId: categoryId),
    );
  }
}

class _PageList extends ConsumerWidget {
  final String categoryId;
  final List<PageModel> pages;

  const _PageList({required this.categoryId, required this.pages});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notifier = ref.read(pageListNotifierProvider(categoryId).notifier);
    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: pages.length,
      separatorBuilder: (_, __) => const SizedBox(height: 10),
      itemBuilder: (context, index) {
        final page = pages[index];
        final summary = PageSummary.fromPageModel(page);
        return PageCard(
          page: summary,
          onTap: () =>
              context.push('/vaults/$categoryId/pages/${page.id}'),
          onToggleFavorite: () => notifier.toggleFavorite(page),
          onDelete: () async {
            final confirm = await showDialog<bool>(
              context: context,
              builder: (ctx) => AlertDialog(
                title: const Text('Delete Page'),
                content: Text('Delete "${page.title}"?'),
                actions: [
                  TextButton(
                    onPressed: () => ctx.pop(false),
                    child: const Text('Cancel'),
                  ),
                  TextButton(
                    onPressed: () => ctx.pop(true),
                    child: const Text('Delete',
                        style: TextStyle(color: AppColors.error)),
                  ),
                ],
              ),
            );
            if (confirm == true) {
              await notifier.deletePage(page.id);
            }
          },
        );
      },
    );
  }
}

class _PageTypePicker extends StatelessWidget {
  final String categoryId;

  const _PageTypePicker({required this.categoryId});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(20),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Choose Page Type',
            style: Theme.of(context)
                .textTheme
                .titleLarge
                ?.copyWith(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 16),
          GridView.count(
            crossAxisCount: 3,
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            mainAxisSpacing: 12,
            crossAxisSpacing: 12,
            childAspectRatio: 1.1,
            children: PageType.values.map((type) {
              return GestureDetector(
                onTap: () {
                  Navigator.of(context).pop();
                  context.push(
                      '/vaults/$categoryId/pages/new?pageType=${type.dbValue}');
                },
                child: Container(
                  decoration: BoxDecoration(
                    color: AppColors.darkCard,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: AppColors.darkDivider),
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(type.emoji, style: const TextStyle(fontSize: 28)),
                      const SizedBox(height: 6),
                      Text(
                        type.label,
                        style: const TextStyle(
                            fontSize: 11, color: AppColors.darkOnSurface),
                        textAlign: TextAlign.center,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
              );
            }).toList(),
          ),
          const SizedBox(height: 8),
        ],
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  final VoidCallback onAdd;

  const _EmptyState({required this.onAdd});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(40),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.note_add_outlined,
                size: 80, color: AppColors.accentLight),
            const SizedBox(height: 20),
            Text('No pages yet', style: theme.textTheme.headlineSmall),
            const SizedBox(height: 10),
            Text(
              'Tap + to add your first page.',
              style: theme.textTheme.bodyMedium
                  ?.copyWith(color: AppColors.darkSubtext),
            ),
            const SizedBox(height: 28),
            ElevatedButton.icon(
              onPressed: onAdd,
              icon: const Icon(Icons.add),
              label: const Text('Add Page'),
            ),
          ],
        ),
      ),
    );
  }
}

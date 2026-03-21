import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/crypto/key_cache.dart';
import '../../core/models/category_model.dart';
import '../../theme/app_colors.dart';
import 'categories_notifier.dart';
import 'category_create_edit_sheet.dart';

class CategoriesScreen extends ConsumerWidget {
  const CategoriesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(categoriesNotifierProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Legacy Vault',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        actions: [
          PopupMenuButton<String>(
            icon: const Icon(Icons.more_vert),
            onSelected: (val) {
              if (val == 'backup') context.push('/backup');
              if (val == 'settings') context.push('/settings');
            },
            itemBuilder: (_) => const [
              PopupMenuItem(value: 'backup', child: Text('Backup & Restore')),
              PopupMenuItem(value: 'settings', child: Text('Settings')),
            ],
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showCreateSheet(context),
        child: const Icon(Icons.add),
      ),
      body: state.isLoading
          ? const Center(child: CircularProgressIndicator())
          : state.categories.isEmpty
              ? _EmptyState(onAdd: () => _showCreateSheet(context))
              : _CategoryList(categories: state.categories),
    );
  }

  void _showCreateSheet(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppColors.darkSurface,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (_) => const CategoryCreateEditSheet(),
    );
  }
}

class _CategoryList extends ConsumerWidget {
  final List<Category> categories;

  const _CategoryList({required this.categories});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: categories.length,
      separatorBuilder: (_, __) => const SizedBox(height: 10),
      itemBuilder: (context, index) {
        final cat = categories[index];
        return _CategoryTile(category: cat);
      },
    );
  }
}

class _CategoryTile extends ConsumerWidget {
  final Category category;

  const _CategoryTile({required this.category});

  void _navigate(BuildContext context) {
    if (category.isEncrypted && !KeyCache.instance.has(category.id)) {
      context.push('/vaults/${category.id}/unlock');
    } else {
      context.push('/vaults/${category.id}/pages');
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final notifier = ref.read(categoriesNotifierProvider.notifier);

    return Card(
      child: InkWell(
        onTap: () => _navigate(context),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          child: Row(
            children: [
              Text(category.icon, style: const TextStyle(fontSize: 28)),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Text(
                          category.name,
                          style: theme.textTheme.titleMedium
                              ?.copyWith(fontWeight: FontWeight.w600),
                        ),
                        if (category.isEncrypted) ...[
                          const SizedBox(width: 6),
                          const Icon(Icons.lock,
                              size: 14, color: AppColors.accentLight),
                        ],
                      ],
                    ),
                    const SizedBox(height: 2),
                    Text(
                      '${category.pageCount} ${category.pageCount == 1 ? 'page' : 'pages'}',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: AppColors.darkSubtext,
                      ),
                    ),
                  ],
                ),
              ),
              if (category.isFavorite)
                const Padding(
                  padding: EdgeInsets.only(right: 4),
                  child: Icon(Icons.star, color: AppColors.warning, size: 18),
                ),
              PopupMenuButton<String>(
                icon: const Icon(Icons.more_vert, size: 20),
                onSelected: (val) async {
                  switch (val) {
                    case 'edit':
                      showModalBottomSheet(
                        context: context,
                        isScrollControlled: true,
                        backgroundColor: AppColors.darkSurface,
                        shape: const RoundedRectangleBorder(
                          borderRadius:
                              BorderRadius.vertical(top: Radius.circular(20)),
                        ),
                        builder: (_) =>
                            CategoryCreateEditSheet(existing: category),
                      );
                    case 'favorite':
                      await notifier.toggleFavorite(category);
                    case 'delete':
                      final confirm = await showDialog<bool>(
                        context: context,
                        builder: (ctx) => AlertDialog(
                          title: const Text('Delete Vault'),
                          content: Text(
                              'Delete "${category.name}" and all its pages?'),
                          actions: [
                            TextButton(
                              onPressed: () => ctx.pop(false),
                              child: const Text('Cancel'),
                            ),
                            TextButton(
                              onPressed: () => ctx.pop(true),
                              child: const Text(
                                'Delete',
                                style: TextStyle(color: AppColors.error),
                              ),
                            ),
                          ],
                        ),
                      );
                      if (confirm == true) {
                        await notifier.deleteCategory(category.id);
                      }
                  }
                },
                itemBuilder: (_) => [
                  const PopupMenuItem(value: 'edit', child: Text('Edit')),
                  PopupMenuItem(
                    value: 'favorite',
                    child: Text(
                        category.isFavorite ? 'Unfavorite' : 'Add to Favorites'),
                  ),
                  const PopupMenuItem(
                    value: 'delete',
                    child:
                        Text('Delete', style: TextStyle(color: AppColors.error)),
                  ),
                ],
              ),
            ],
          ),
        ),
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
            const Icon(Icons.lock_outline, size: 80, color: AppColors.accentLight),
            const SizedBox(height: 20),
            Text(
              'No vaults yet',
              style: theme.textTheme.headlineSmall,
            ),
            const SizedBox(height: 10),
            Text(
              'Create your first vault to start storing your legacy.',
              textAlign: TextAlign.center,
              style: theme.textTheme.bodyMedium
                  ?.copyWith(color: AppColors.darkSubtext),
            ),
            const SizedBox(height: 28),
            ElevatedButton.icon(
              onPressed: onAdd,
              icon: const Icon(Icons.add),
              label: const Text('Create Vault'),
            ),
          ],
        ),
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/models/page_content.dart';
import '../../core/models/page_model.dart';
import '../../core/models/shopping_list_item.dart';
import '../../theme/app_colors.dart';
import 'shopping_notifier.dart';

class ShoppingScreen extends ConsumerWidget {
  const ShoppingScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(shoppingNotifierProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Shopping',
            style: TextStyle(fontWeight: FontWeight.bold)),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () async {
              await ref
                  .read(shoppingNotifierProvider.notifier)
                  .loadShoppingListsAsync();
            },
          ),
        ],
      ),
      body: state.isLoading
          ? const Center(child: CircularProgressIndicator())
          : state.lists.isEmpty
              ? _EmptyState()
              : _ShoppingLists(lists: state.lists),
    );
  }
}

class _ShoppingLists extends ConsumerWidget {
  final List<({PageModel page, ShoppingListContent content})> lists;

  const _ShoppingLists({required this.lists});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notifier = ref.read(shoppingNotifierProvider.notifier);

    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: lists.length,
      separatorBuilder: (_, __) => const SizedBox(height: 16),
      itemBuilder: (context, index) {
        final list = lists[index];
        return _ShoppingListCard(
          page: list.page,
          content: list.content,
          onToggleItem: (itemIndex) =>
              notifier.toggleItemAsync(list.page, list.content, itemIndex),
        );
      },
    );
  }
}

class _ShoppingListCard extends StatelessWidget {
  final PageModel page;
  final ShoppingListContent content;
  final ValueChanged<int> onToggleItem;

  const _ShoppingListCard({
    required this.page,
    required this.content,
    required this.onToggleItem,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final checkedCount = content.items.where((i) => i.checked).length;
    final total = content.items.length;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Header
        InkWell(
          onTap: () => context.push('/vaults/${page.categoryId}/pages/${page.id}'),
          borderRadius: BorderRadius.circular(12),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
            decoration: BoxDecoration(
              color: AppColors.darkCard,
              borderRadius: const BorderRadius.vertical(top: Radius.circular(12)),
              border: const Border(
                top: BorderSide(color: AppColors.accent, width: 2),
                left: BorderSide(color: AppColors.darkDivider),
                right: BorderSide(color: AppColors.darkDivider),
              ),
            ),
            child: Row(
              children: [
                const Icon(Icons.shopping_cart_outlined,
                    color: AppColors.accentLight, size: 18),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    page.title,
                    style: theme.textTheme.titleMedium
                        ?.copyWith(fontWeight: FontWeight.bold),
                  ),
                ),
                Text(
                  '$checkedCount / $total',
                  style: const TextStyle(
                      color: AppColors.darkSubtext, fontSize: 13),
                ),
                const SizedBox(width: 4),
                const Icon(Icons.open_in_new, size: 14, color: AppColors.darkSubtext),
              ],
            ),
          ),
        ),
        // Progress bar
        if (total > 0)
          LinearProgressIndicator(
            value: total > 0 ? checkedCount / total : 0,
            backgroundColor: AppColors.darkDivider,
            valueColor:
                const AlwaysStoppedAnimation<Color>(AppColors.accent),
            minHeight: 3,
          ),
        // Items
        Container(
          decoration: BoxDecoration(
            color: AppColors.darkCard,
            borderRadius:
                const BorderRadius.vertical(bottom: Radius.circular(12)),
            border: Border.all(color: AppColors.darkDivider),
          ),
          child: content.items.isEmpty
              ? const Padding(
                  padding: EdgeInsets.all(14),
                  child: Text(
                    'No items in this list.',
                    style: TextStyle(color: AppColors.darkSubtext),
                  ),
                )
              : Column(
                  children: content.items.asMap().entries.map((e) {
                    final idx = e.key;
                    final item = e.value;
                    return Column(
                      children: [
                        _ShoppingItemRow(
                          item: item,
                          onToggle: () => onToggleItem(idx),
                        ),
                        if (idx < content.items.length - 1)
                          const Divider(height: 1, indent: 16, endIndent: 16),
                      ],
                    );
                  }).toList(),
                ),
        ),
      ],
    );
  }
}

class _ShoppingItemRow extends StatelessWidget {
  final ShoppingListItem item;
  final VoidCallback onToggle;

  const _ShoppingItemRow({required this.item, required this.onToggle});

  @override
  Widget build(BuildContext context) {
    return ListTile(
      dense: true,
      leading: Checkbox(
        value: item.checked,
        onChanged: (_) => onToggle(),
        activeColor: AppColors.accent,
      ),
      title: Text(
        item.name,
        style: TextStyle(
          decoration: item.checked ? TextDecoration.lineThrough : null,
          color: item.checked ? AppColors.darkSubtext : null,
        ),
      ),
      trailing: item.quantity.isNotEmpty
          ? Text(
              item.quantity,
              style:
                  const TextStyle(color: AppColors.darkSubtext, fontSize: 13),
            )
          : null,
    );
  }
}

class _EmptyState extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.shopping_cart_outlined,
              size: 72, color: AppColors.accentLight),
          const SizedBox(height: 20),
          Text('No shopping lists',
              style: Theme.of(context).textTheme.headlineSmall),
          const SizedBox(height: 10),
          Text(
            'Create Shopping List pages in any vault to see them here.',
            textAlign: TextAlign.center,
            style: Theme.of(context)
                .textTheme
                .bodyMedium
                ?.copyWith(color: AppColors.darkSubtext),
          ),
        ],
      ),
    );
  }
}

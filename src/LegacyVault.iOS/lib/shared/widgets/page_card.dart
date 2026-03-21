import 'package:flutter/material.dart';

import '../../core/models/page_model.dart';
import '../../core/models/page_type.dart';
import '../../theme/app_colors.dart';

class PageCard extends StatelessWidget {
  final PageSummary page;
  final VoidCallback onTap;
  final VoidCallback? onDelete;
  final VoidCallback? onToggleFavorite;

  const PageCard({
    super.key,
    required this.page,
    required this.onTap,
    this.onDelete,
    this.onToggleFavorite,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Row(
            children: [
              _TypeIcon(type: page.type),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      page.title,
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 4),
                    Row(
                      children: [
                        Text(
                          page.type.emoji,
                          style: const TextStyle(fontSize: 12),
                        ),
                        const SizedBox(width: 4),
                        Text(
                          page.type.label,
                          style: theme.textTheme.bodySmall?.copyWith(
                            color: AppColors.accentLight,
                          ),
                        ),
                        if (page.isEncrypted) ...[
                          const SizedBox(width: 8),
                          const Icon(
                            Icons.lock,
                            size: 12,
                            color: AppColors.accentLight,
                          ),
                        ],
                      ],
                    ),
                  ],
                ),
              ),
              if (onToggleFavorite != null)
                IconButton(
                  icon: Icon(
                    page.isFavorite ? Icons.star : Icons.star_border,
                    color: page.isFavorite
                        ? AppColors.warning
                        : theme.iconTheme.color,
                    size: 20,
                  ),
                  onPressed: onToggleFavorite,
                  padding: EdgeInsets.zero,
                  constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
                ),
              if (onDelete != null)
                IconButton(
                  icon: const Icon(Icons.delete_outline, size: 20),
                  onPressed: onDelete,
                  padding: EdgeInsets.zero,
                  constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
                  color: AppColors.error,
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _TypeIcon extends StatelessWidget {
  final PageType type;

  const _TypeIcon({required this.type});

  Color _iconBg() {
    switch (type) {
      case PageType.note:
        return const Color(0xFF1E3A5F);
      case PageType.password:
        return const Color(0xFF3B1A1A);
      case PageType.recipe:
        return const Color(0xFF1A3B1A);
      case PageType.quote:
        return const Color(0xFF2D1A3B);
      case PageType.homeInventory:
        return const Color(0xFF1A2D3B);
      case PageType.reminder:
        return const Color(0xFF3B2D1A);
      case PageType.shoppingList:
        return const Color(0xFF1A3B2D);
    }
  }

  Color _iconColor() {
    switch (type) {
      case PageType.note:
        return const Color(0xFF60A5FA);
      case PageType.password:
        return const Color(0xFFF87171);
      case PageType.recipe:
        return const Color(0xFF4ADE80);
      case PageType.quote:
        return const Color(0xFFA78BFA);
      case PageType.homeInventory:
        return const Color(0xFF38BDF8);
      case PageType.reminder:
        return const Color(0xFFFBBF24);
      case PageType.shoppingList:
        return const Color(0xFF34D399);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 44,
      height: 44,
      decoration: BoxDecoration(
        color: _iconBg(),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Icon(type.icon, color: _iconColor(), size: 22),
    );
  }
}

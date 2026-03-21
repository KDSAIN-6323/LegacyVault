import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../../core/models/page_content.dart';
import '../../core/models/page_model.dart';
import '../../theme/app_colors.dart';
import 'reminders_notifier.dart';

class RemindersScreen extends ConsumerWidget {
  const RemindersScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(remindersNotifierProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Reminders',
            style: TextStyle(fontWeight: FontWeight.bold)),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () async {
              await ref
                  .read(remindersNotifierProvider.notifier)
                  .loadRemindersAsync();
            },
          ),
        ],
      ),
      body: state.isLoading
          ? const Center(child: CircularProgressIndicator())
          : state.reminders.isEmpty
              ? _EmptyState()
              : _ReminderList(reminders: state.reminders),
    );
  }
}

class _ReminderList extends StatelessWidget {
  final List<({PageModel page, ReminderContent content})> reminders;

  const _ReminderList({required this.reminders});

  @override
  Widget build(BuildContext context) {
    final now = DateTime.now();

    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: reminders.length,
      separatorBuilder: (_, __) => const SizedBox(height: 10),
      itemBuilder: (context, index) {
        final reminder = reminders[index];
        return _ReminderCard(
          page: reminder.page,
          content: reminder.content,
          isOverdue: _isOverdue(reminder.content.date, now),
          isToday: _isToday(reminder.content.date, now),
        );
      },
    );
  }

  bool _isOverdue(String dateStr, DateTime now) {
    try {
      final date = DateTime.parse(dateStr);
      return date.isBefore(now);
    } catch (_) {
      return false;
    }
  }

  bool _isToday(String dateStr, DateTime now) {
    try {
      final date = DateTime.parse(dateStr);
      return date.year == now.year &&
          date.month == now.month &&
          date.day == now.day;
    } catch (_) {
      return false;
    }
  }
}

class _ReminderCard extends StatelessWidget {
  final PageModel page;
  final ReminderContent content;
  final bool isOverdue;
  final bool isToday;

  const _ReminderCard({
    required this.page,
    required this.content,
    required this.isOverdue,
    required this.isToday,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final dateStr = _formatDate(content.date);

    Color borderColor = AppColors.darkDivider;
    if (isOverdue) borderColor = AppColors.error;
    if (isToday) borderColor = AppColors.warning;

    return InkWell(
      onTap: () => context.push('/vaults/${page.categoryId}/pages/${page.id}'),
      borderRadius: BorderRadius.circular(12),
      child: Container(
        decoration: BoxDecoration(
          color: AppColors.darkCard,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: borderColor, width: isOverdue || isToday ? 1.5 : 1),
        ),
        padding: const EdgeInsets.all(14),
        child: Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: isOverdue
                    ? AppColors.error.withAlpha(40)
                    : isToday
                        ? AppColors.warning.withAlpha(40)
                        : const Color(0xFF3B2D1A),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Icon(
                Icons.alarm,
                color: isOverdue
                    ? AppColors.error
                    : isToday
                        ? AppColors.warning
                        : AppColors.warning,
                size: 22,
              ),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    page.title,
                    style: theme.textTheme.titleMedium
                        ?.copyWith(fontWeight: FontWeight.w600),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 3),
                  Row(
                    children: [
                      Icon(
                        Icons.access_time,
                        size: 13,
                        color: isOverdue
                            ? AppColors.error
                            : AppColors.darkSubtext,
                      ),
                      const SizedBox(width: 4),
                      Text(
                        dateStr,
                        style: TextStyle(
                          fontSize: 13,
                          color: isOverdue ? AppColors.error : AppColors.darkSubtext,
                          fontWeight: isToday ? FontWeight.w600 : FontWeight.normal,
                        ),
                      ),
                      if (content.recurrence != 'NONE') ...[
                        const SizedBox(width: 8),
                        const Icon(Icons.repeat, size: 13, color: AppColors.accentLight),
                        const SizedBox(width: 2),
                        Text(
                          content.recurrence,
                          style: const TextStyle(
                              fontSize: 11, color: AppColors.accentLight),
                        ),
                      ],
                    ],
                  ),
                  if (content.tag.isNotEmpty) ...[
                    const SizedBox(height: 4),
                    Container(
                      padding:
                          const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                      decoration: BoxDecoration(
                        color: AppColors.accent.withAlpha(40),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        content.tag,
                        style: const TextStyle(
                            fontSize: 11, color: AppColors.accentLight),
                      ),
                    ),
                  ],
                ],
              ),
            ),
            if (isOverdue)
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: AppColors.error.withAlpha(40),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Text(
                  'OVERDUE',
                  style: TextStyle(
                      fontSize: 10,
                      color: AppColors.error,
                      fontWeight: FontWeight.bold),
                ),
              )
            else if (isToday)
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: AppColors.warning.withAlpha(40),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Text(
                  'TODAY',
                  style: TextStyle(
                      fontSize: 10,
                      color: AppColors.warning,
                      fontWeight: FontWeight.bold),
                ),
              ),
          ],
        ),
      ),
    );
  }

  String _formatDate(String dateStr) {
    try {
      final dt = DateTime.parse(dateStr);
      return DateFormat('MMM d, yyyy • h:mm a').format(dt);
    } catch (_) {
      return dateStr;
    }
  }
}

class _EmptyState extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.alarm_off_outlined,
              size: 72, color: AppColors.accentLight),
          const SizedBox(height: 20),
          Text('No reminders',
              style: Theme.of(context).textTheme.headlineSmall),
          const SizedBox(height: 10),
          Text(
            'Create Reminder pages in any vault to see them here.',
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

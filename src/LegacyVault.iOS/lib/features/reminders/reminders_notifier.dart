import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/models/page_content.dart';
import '../../core/models/page_model.dart';
import '../../core/models/page_type.dart';
import '../../core/repositories/page_repository.dart';
import '../pages/page_list_notifier.dart';

class RemindersState {
  final List<({PageModel page, ReminderContent content})> reminders;
  final bool isLoading;
  final String? error;

  const RemindersState({
    this.reminders = const [],
    this.isLoading = false,
    this.error,
  });

  RemindersState copyWith({
    List<({PageModel page, ReminderContent content})>? reminders,
    bool? isLoading,
    String? error,
  }) =>
      RemindersState(
        reminders: reminders ?? this.reminders,
        isLoading: isLoading ?? this.isLoading,
        error: error,
      );
}

class RemindersNotifier extends StateNotifier<RemindersState> {
  final PageRepository _repo;

  RemindersNotifier(this._repo) : super(const RemindersState()) {
    loadRemindersAsync();
  }

  Future<void> loadRemindersAsync() async {
    state = state.copyWith(isLoading: true);
    try {
      final pages = await _repo.getPagesByTypeAsync(PageType.reminder);
      final reminders = <({PageModel page, ReminderContent content})>[];

      for (final page in pages) {
        final content = _repo.decryptAndParseContent(page);
        if (content is ReminderContent) {
          reminders.add((page: page, content: content));
        } else if (!page.isEncrypted) {
          // Try to parse as reminder even if content can't be decrypted
          try {
            final c = PageContent.fromJson(page.type.dbValue, page.content);
            if (c is ReminderContent) {
              reminders.add((page: page, content: c));
            }
          } catch (_) {}
        }
      }

      // Sort by date
      reminders.sort((a, b) => a.content.date.compareTo(b.content.date));

      state = RemindersState(reminders: reminders);
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }
}

final remindersNotifierProvider =
    StateNotifierProvider<RemindersNotifier, RemindersState>((ref) {
  return RemindersNotifier(ref.watch(pageRepositoryProvider));
});

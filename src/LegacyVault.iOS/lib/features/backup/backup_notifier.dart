import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/repositories/backup_repository.dart';

enum BackupStatus { idle, loading, success, error }

class BackupState {
  final BackupStatus status;
  final String? message;

  const BackupState({
    this.status = BackupStatus.idle,
    this.message,
  });

  BackupState copyWith({BackupStatus? status, String? message}) =>
      BackupState(
        status: status ?? this.status,
        message: message,
      );
}

class BackupNotifier extends StateNotifier<BackupState> {
  final BackupRepository _repo;

  BackupNotifier(this._repo) : super(const BackupState());

  Future<void> exportBackup() async {
    state = state.copyWith(status: BackupStatus.loading);
    try {
      final path = await _repo.exportBackupToFile();
      state = BackupState(
        status: BackupStatus.success,
        message: 'Backup saved to:\n$path',
      );
    } catch (e) {
      state = BackupState(
        status: BackupStatus.error,
        message: 'Export failed: $e',
      );
    }
  }

  Future<void> importBackup() async {
    state = state.copyWith(status: BackupStatus.loading);
    try {
      final data = await _repo.importBackupFromFile();
      await _repo.restoreBackup(data);
      state = BackupState(
        status: BackupStatus.success,
        message:
            'Restore complete. Imported ${data.categories.length} vaults and ${data.pages.length} pages.',
      );
    } catch (e) {
      if (e.toString().contains('No file selected')) {
        state = const BackupState(status: BackupStatus.idle);
      } else {
        state = BackupState(
          status: BackupStatus.error,
          message: 'Import failed: $e',
        );
      }
    }
  }

  void reset() => state = const BackupState();
}

final backupRepositoryProvider = Provider<BackupRepository>(
  (_) => BackupRepository(),
);

final backupNotifierProvider =
    StateNotifierProvider<BackupNotifier, BackupState>((ref) {
  return BackupNotifier(ref.watch(backupRepositoryProvider));
});

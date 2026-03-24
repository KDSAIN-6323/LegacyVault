import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../theme/app_colors.dart';
import 'backup_notifier.dart';

class BackupScreen extends ConsumerWidget {
  const BackupScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(backupNotifierProvider);
    final notifier = ref.read(backupNotifierProvider.notifier);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Backup & Restore',
            style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Info card
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: AppColors.accent.withAlpha(30),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: AppColors.accent.withAlpha(80)),
              ),
              child: const Row(
                children: [
                  Icon(Icons.info_outline, color: AppColors.accentLight),
                  SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'Backups include all vaults and pages. Encrypted vault data is backed up as-is (still encrypted). Import merges data without deleting existing entries.',
                      style: TextStyle(
                          color: AppColors.accentLight, fontSize: 13),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 32),

            // Export
            _ActionCard(
              icon: Icons.upload_outlined,
              title: 'Export Backup',
              subtitle: 'Save all vault data to a JSON file on this device',
              buttonLabel: 'Export',
              buttonIcon: Icons.upload,
              onPressed: state.status == BackupStatus.loading
                  ? null
                  : () => notifier.exportBackup(),
            ),
            const SizedBox(height: 20),

            // Import
            _ActionCard(
              icon: Icons.download_outlined,
              title: 'Restore Backup',
              subtitle:
                  'Import vault data from a previously exported JSON file',
              buttonLabel: 'Import',
              buttonIcon: Icons.download,
              isDestructive: true,
              onPressed: state.status == BackupStatus.loading
                  ? null
                  : () => _confirmImport(context, notifier),
            ),

            // Status
            if (state.status == BackupStatus.loading) ...[
              const SizedBox(height: 32),
              const Center(child: CircularProgressIndicator()),
            ],

            if (state.status == BackupStatus.success && state.message != null) ...[
              const SizedBox(height: 24),
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.success.withAlpha(30),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.success.withAlpha(80)),
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Icon(Icons.check_circle_outline,
                        color: AppColors.success, size: 20),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        state.message!,
                        style: const TextStyle(color: AppColors.success),
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close, size: 16),
                      onPressed: notifier.reset,
                      color: AppColors.success,
                      padding: EdgeInsets.zero,
                    ),
                  ],
                ),
              ),
            ],

            if (state.status == BackupStatus.error && state.message != null) ...[
              const SizedBox(height: 24),
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.error.withAlpha(30),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.error.withAlpha(80)),
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Icon(Icons.error_outline,
                        color: AppColors.error, size: 20),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        state.message!,
                        style: const TextStyle(color: AppColors.error),
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close, size: 16),
                      onPressed: notifier.reset,
                      color: AppColors.error,
                      padding: EdgeInsets.zero,
                    ),
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Future<void> _confirmImport(
      BuildContext context, BackupNotifier notifier) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Restore Backup'),
        content: const Text(
          'This will import data from the selected file and merge it with existing data. Continue?',
        ),
        actions: [
          TextButton(
            onPressed: () => ctx.pop(false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () => ctx.pop(true),
            child: const Text('Import'),
          ),
        ],
      ),
    );
    if (confirmed == true) {
      await notifier.importBackup();
    }
  }
}

class _ActionCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final String buttonLabel;
  final IconData buttonIcon;
  final VoidCallback? onPressed;
  final bool isDestructive;

  const _ActionCard({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.buttonLabel,
    required this.buttonIcon,
    required this.onPressed,
    this.isDestructive = false,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.darkCard,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.darkDivider),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: AppColors.accentLight, size: 28),
              const SizedBox(width: 12),
              Text(
                title,
                style: Theme.of(context)
                    .textTheme
                    .titleMedium
                    ?.copyWith(fontWeight: FontWeight.bold),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            subtitle,
            style: Theme.of(context)
                .textTheme
                .bodySmall
                ?.copyWith(color: AppColors.darkSubtext),
          ),
          const SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton.icon(
              onPressed: onPressed,
              icon: Icon(buttonIcon, size: 18),
              label: Text(buttonLabel),
              style: isDestructive
                  ? ElevatedButton.styleFrom(
                      backgroundColor: AppColors.warning,
                      foregroundColor: Colors.white,
                    )
                  : null,
            ),
          ),
        ],
      ),
    );
  }
}

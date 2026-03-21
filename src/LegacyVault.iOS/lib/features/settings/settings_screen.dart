import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/crypto/key_cache.dart';
import '../../core/preferences/user_preferences.dart';
import '../../theme/app_colors.dart';
import 'settings_notifier.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(settingsNotifierProvider);
    final notifier = ref.read(settingsNotifierProvider.notifier);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title:
            const Text('Settings', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Appearance section
          _SectionHeader(title: 'Appearance'),
          const SizedBox(height: 8),
          _SettingsCard(
            children: [
              _SettingsTile(
                icon: Icons.palette_outlined,
                title: 'Theme',
                subtitle: _themeLabel(state.theme),
                onTap: () => _showThemePicker(context, notifier, state.theme),
              ),
              const Divider(height: 1),
              _SettingsTile(
                icon: Icons.text_fields,
                title: 'Font Size',
                subtitle: _fontSizeLabel(state.fontSize),
                onTap: () =>
                    _showFontSizePicker(context, notifier, state.fontSize),
              ),
            ],
          ),
          const SizedBox(height: 20),

          // Security section
          _SectionHeader(title: 'Security'),
          const SizedBox(height: 8),
          _SettingsCard(
            children: [
              _SettingsTile(
                icon: Icons.timer_outlined,
                title: 'Auto-Lock',
                subtitle:
                    'Lock after ${state.inactivityMinutes} minute${state.inactivityMinutes == 1 ? '' : 's'} of inactivity',
                onTap: () =>
                    _showInactivityPicker(context, notifier, state.inactivityMinutes),
              ),
              const Divider(height: 1),
              _SettingsTile(
                icon: Icons.lock_reset,
                title: 'Lock All Vaults Now',
                subtitle: 'Clear all cached vault keys',
                onTap: () {
                  KeyCache.instance.clearAll();
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('All vaults locked')),
                  );
                },
                textColor: AppColors.error,
              ),
            ],
          ),
          const SizedBox(height: 20),

          // About section
          _SectionHeader(title: 'About'),
          const SizedBox(height: 8),
          _SettingsCard(
            children: [
              _SettingsTile(
                icon: Icons.info_outline,
                title: 'App Version',
                subtitle: '1.0.0',
              ),
              const Divider(height: 1),
              _SettingsTile(
                icon: Icons.shield_outlined,
                title: 'Encryption',
                subtitle: 'AES-256-GCM with PBKDF2-SHA256 (310,000 iterations)',
              ),
            ],
          ),
        ],
      ),
    );
  }

  String _themeLabel(AppTheme theme) {
    switch (theme) {
      case AppTheme.dark:
        return 'Dark';
      case AppTheme.light:
        return 'Light';
      case AppTheme.system:
        return 'System';
    }
  }

  String _fontSizeLabel(FontSize size) {
    switch (size) {
      case FontSize.small:
        return 'Small';
      case FontSize.medium:
        return 'Medium';
      case FontSize.large:
        return 'Large';
    }
  }

  void _showThemePicker(
      BuildContext context, SettingsNotifier notifier, AppTheme current) {
    showDialog(
      context: context,
      builder: (ctx) => SimpleDialog(
        title: const Text('Choose Theme'),
        children: AppTheme.values
            .map((t) => SimpleDialogOption(
                  onPressed: () {
                    notifier.setTheme(t);
                    Navigator.of(ctx).pop();
                  },
                  child: Row(
                    children: [
                      if (t == current)
                        const Icon(Icons.check, color: AppColors.accent, size: 18),
                      if (t != current) const SizedBox(width: 18),
                      const SizedBox(width: 8),
                      Text(_themeLabel(t)),
                    ],
                  ),
                ))
            .toList(),
      ),
    );
  }

  void _showFontSizePicker(
      BuildContext context, SettingsNotifier notifier, FontSize current) {
    showDialog(
      context: context,
      builder: (ctx) => SimpleDialog(
        title: const Text('Font Size'),
        children: FontSize.values
            .map((s) => SimpleDialogOption(
                  onPressed: () {
                    notifier.setFontSize(s);
                    Navigator.of(ctx).pop();
                  },
                  child: Row(
                    children: [
                      if (s == current)
                        const Icon(Icons.check, color: AppColors.accent, size: 18),
                      if (s != current) const SizedBox(width: 18),
                      const SizedBox(width: 8),
                      Text(_fontSizeLabel(s)),
                    ],
                  ),
                ))
            .toList(),
      ),
    );
  }

  void _showInactivityPicker(
      BuildContext context, SettingsNotifier notifier, int current) {
    const options = [1, 2, 5, 10, 15, 30, 60];
    showDialog(
      context: context,
      builder: (ctx) => SimpleDialog(
        title: const Text('Auto-Lock After'),
        children: options
            .map((minutes) => SimpleDialogOption(
                  onPressed: () {
                    notifier.setInactivityMinutes(minutes);
                    Navigator.of(ctx).pop();
                  },
                  child: Row(
                    children: [
                      if (minutes == current)
                        const Icon(Icons.check, color: AppColors.accent, size: 18),
                      if (minutes != current) const SizedBox(width: 18),
                      const SizedBox(width: 8),
                      Text(
                          '$minutes minute${minutes == 1 ? '' : 's'}'),
                    ],
                  ),
                ))
            .toList(),
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final String title;

  const _SectionHeader({required this.title});

  @override
  Widget build(BuildContext context) {
    return Text(
      title.toUpperCase(),
      style: const TextStyle(
        fontSize: 12,
        fontWeight: FontWeight.w600,
        color: AppColors.accentLight,
        letterSpacing: 1.2,
      ),
    );
  }
}

class _SettingsCard extends StatelessWidget {
  final List<Widget> children;

  const _SettingsCard({required this.children});

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.darkCard,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.darkDivider),
      ),
      child: Column(children: children),
    );
  }
}

class _SettingsTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String? subtitle;
  final VoidCallback? onTap;
  final Color? textColor;

  const _SettingsTile({
    required this.icon,
    required this.title,
    this.subtitle,
    this.onTap,
    this.textColor,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Icon(icon, color: textColor ?? AppColors.accentLight, size: 22),
      title: Text(
        title,
        style: TextStyle(color: textColor, fontWeight: FontWeight.w500),
      ),
      subtitle: subtitle != null
          ? Text(subtitle!,
              style: const TextStyle(
                  color: AppColors.darkSubtext, fontSize: 13))
          : null,
      trailing: onTap != null
          ? const Icon(Icons.chevron_right, color: AppColors.darkSubtext, size: 20)
          : null,
      onTap: onTap,
    );
  }
}

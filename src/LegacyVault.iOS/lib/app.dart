import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'core/preferences/user_preferences.dart';
import 'features/settings/settings_notifier.dart';
import 'navigation/app_router.dart';
import 'theme/app_theme.dart' as themes;

class LegacyVaultApp extends ConsumerWidget {
  const LegacyVaultApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final settings = ref.watch(settingsNotifierProvider);
    final prefs = ref.watch(userPreferencesProvider);

    final fontScale = prefs.fontScaleFactor;
    final lightTheme = themes.AppTheme.light(fontScaleFactor: fontScale);
    final darkTheme = themes.AppTheme.dark(fontScaleFactor: fontScale);

    final ThemeMode themeMode;
    switch (settings.theme) {
      case AppTheme.light:
        themeMode = ThemeMode.light;
      case AppTheme.dark:
        themeMode = ThemeMode.dark;
      case AppTheme.system:
      default:
        themeMode = ThemeMode.system;
    }

    return MaterialApp.router(
      title: 'Legacy Vault',
      debugShowCheckedModeBanner: false,
      theme: lightTheme,
      darkTheme: darkTheme,
      themeMode: themeMode,
      routerConfig: appRouter,
    );
  }
}

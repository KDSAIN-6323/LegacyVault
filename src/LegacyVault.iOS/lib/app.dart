import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'core/preferences/user_preferences.dart' hide AppTheme;
import 'features/settings/settings_notifier.dart';
import 'navigation/app_router.dart';
import 'theme/app_theme.dart';

class LegacyVaultApp extends ConsumerWidget {
  const LegacyVaultApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final settings = ref.watch(settingsNotifierProvider);
    final prefs = ref.watch(userPreferencesProvider);

    final fontScale = prefs.fontScaleFactor;
    final lightTheme = AppTheme.light(fontScaleFactor: fontScale);
    final darkTheme = AppTheme.dark(fontScaleFactor: fontScale);

    final ThemeMode themeMode;
    switch (settings.theme) {
      case AppTheme.light:
        themeMode = ThemeMode.light;
      case AppTheme.dark:
        themeMode = ThemeMode.dark;
      default:
        themeMode = ThemeMode.system;
      //case AppTheme.system:
        //themeMode = ThemeMode.system;
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

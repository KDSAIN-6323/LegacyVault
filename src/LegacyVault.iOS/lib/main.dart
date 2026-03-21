import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app.dart';
import 'core/preferences/user_preferences.dart';
import 'features/settings/settings_notifier.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final prefs = await UserPreferences.create();

  runApp(
    ProviderScope(
      overrides: [
        userPreferencesProvider.overrideWithValue(prefs),
      ],
      child: const LegacyVaultApp(),
    ),
  );
}

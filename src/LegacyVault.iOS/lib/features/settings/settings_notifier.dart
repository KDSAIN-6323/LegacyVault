import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/preferences/user_preferences.dart';

class SettingsState {
  final AppTheme theme;
  final FontSize fontSize;
  final int inactivityMinutes;

  const SettingsState({
    this.theme = AppTheme.dark,
    this.fontSize = FontSize.medium,
    this.inactivityMinutes = 5,
  });

  SettingsState copyWith({
    AppTheme? theme,
    FontSize? fontSize,
    int? inactivityMinutes,
  }) =>
      SettingsState(
        theme: theme ?? this.theme,
        fontSize: fontSize ?? this.fontSize,
        inactivityMinutes: inactivityMinutes ?? this.inactivityMinutes,
      );
}

class SettingsNotifier extends StateNotifier<SettingsState> {
  final UserPreferences _prefs;

  SettingsNotifier(this._prefs)
      : super(SettingsState(
          theme: _prefs.theme,
          fontSize: _prefs.fontSize,
          inactivityMinutes: _prefs.inactivityMinutes,
        ));

  Future<void> setTheme(AppTheme theme) async {
    await _prefs.setTheme(theme);
    state = state.copyWith(theme: theme);
  }

  Future<void> setFontSize(FontSize size) async {
    await _prefs.setFontSize(size);
    state = state.copyWith(fontSize: size);
  }

  Future<void> setInactivityMinutes(int minutes) async {
    await _prefs.setInactivityMinutes(minutes);
    state = state.copyWith(inactivityMinutes: minutes);
  }
}

final userPreferencesProvider = Provider<UserPreferences>((ref) {
  throw UnimplementedError('Initialize with override in main');
});

final settingsNotifierProvider =
    StateNotifierProvider<SettingsNotifier, SettingsState>((ref) {
  return SettingsNotifier(ref.watch(userPreferencesProvider));
});

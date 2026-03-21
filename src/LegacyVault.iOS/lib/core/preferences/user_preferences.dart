import 'package:shared_preferences/shared_preferences.dart';

enum AppTheme { system, dark, light }

enum FontSize { small, medium, large }

class UserPreferences {
  static const _keyTheme = 'theme';
  static const _keyFontSize = 'font_size';
  static const _keyInactivityMinutes = 'inactivity_minutes';

  final SharedPreferences _prefs;

  UserPreferences(this._prefs);

  static Future<UserPreferences> create() async {
    final prefs = await SharedPreferences.getInstance();
    return UserPreferences(prefs);
  }

  // ─── Theme ───────────────────────────────────────────────────────────────────

  AppTheme get theme {
    final val = _prefs.getString(_keyTheme);
    switch (val) {
      case 'light':
        return AppTheme.light;
      case 'dark':
        return AppTheme.dark;
      default:
        return AppTheme.dark;
    }
  }

  Future<void> setTheme(AppTheme theme) async {
    await _prefs.setString(_keyTheme, theme.name);
  }

  // ─── Font size ───────────────────────────────────────────────────────────────

  FontSize get fontSize {
    final val = _prefs.getString(_keyFontSize);
    switch (val) {
      case 'small':
        return FontSize.small;
      case 'large':
        return FontSize.large;
      default:
        return FontSize.medium;
    }
  }

  Future<void> setFontSize(FontSize size) async {
    await _prefs.setString(_keyFontSize, size.name);
  }

  double get fontScaleFactor {
    switch (fontSize) {
      case FontSize.small:
        return 0.85;
      case FontSize.large:
        return 1.15;
      case FontSize.medium:
        return 1.0;
    }
  }

  // ─── Inactivity lock ─────────────────────────────────────────────────────────

  int get inactivityMinutes => _prefs.getInt(_keyInactivityMinutes) ?? 5;

  Future<void> setInactivityMinutes(int minutes) async {
    await _prefs.setInt(_keyInactivityMinutes, minutes);
  }
}

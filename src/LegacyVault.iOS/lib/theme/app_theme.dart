import 'package:flutter/material.dart';
import 'app_colors.dart';

class AppTheme {
  AppTheme._();

  static ThemeData dark({double fontScaleFactor = 1.0}) {
    final base = ThemeData.dark();
    return base.copyWith(
      colorScheme: const ColorScheme.dark(
        primary: AppColors.accent,
        secondary: AppColors.accentLight,
        surface: AppColors.darkSurface,
        error: AppColors.error,
        onPrimary: Colors.white,
        onSecondary: Colors.white,
        onSurface: AppColors.darkOnSurface,
        onError: Colors.white,
      ),
      scaffoldBackgroundColor: AppColors.darkBackground,
      appBarTheme: const AppBarTheme(
        backgroundColor: AppColors.darkSurface,
        foregroundColor: AppColors.darkOnBackground,
        elevation: 0,
        centerTitle: false,
      ),
      cardTheme: CardThemeData(
        color: AppColors.darkCard,
        elevation: 0,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        margin: EdgeInsets.zero,
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: AppColors.accent,
        foregroundColor: Colors.white,
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.darkCard,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: AppColors.darkDivider),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: AppColors.darkDivider),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: AppColors.accent, width: 2),
        ),
        labelStyle: const TextStyle(color: AppColors.darkSubtext),
        hintStyle: const TextStyle(color: AppColors.darkSubtext),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.accent,
          foregroundColor: Colors.white,
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: AppColors.accentLight,
        ),
      ),
      dividerTheme: const DividerThemeData(
        color: AppColors.darkDivider,
        thickness: 1,
      ),
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: AppColors.darkSurface,
        selectedItemColor: AppColors.accentLight,
        unselectedItemColor: AppColors.darkSubtext,
        type: BottomNavigationBarType.fixed,
        elevation: 8,
      ),
      chipTheme: ChipThemeData(
        backgroundColor: AppColors.darkCard,
        selectedColor: AppColors.accent,
        labelStyle: const TextStyle(color: AppColors.darkOnSurface),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      textTheme: _buildTextTheme(base.textTheme, AppColors.darkOnBackground, fontScaleFactor),
      iconTheme: const IconThemeData(color: AppColors.darkOnSurface),
      switchTheme: SwitchThemeData(
        thumbColor: WidgetStateProperty.resolveWith((states) =>
            states.contains(WidgetState.selected)
                ? AppColors.accent
                : AppColors.darkSubtext),
        trackColor: WidgetStateProperty.resolveWith((states) =>
            states.contains(WidgetState.selected)
                ? AppColors.accentDark
                : AppColors.darkCard),
      ),
    );
  }

  static ThemeData light({double fontScaleFactor = 1.0}) {
    final base = ThemeData.light();
    return base.copyWith(
      colorScheme: const ColorScheme.light(
        primary: AppColors.accent,
        secondary: AppColors.accentDark,
        surface: AppColors.lightSurface,
        error: AppColors.error,
        onPrimary: Colors.white,
        onSecondary: Colors.white,
        onSurface: AppColors.lightOnSurface,
        onError: Colors.white,
      ),
      scaffoldBackgroundColor: AppColors.lightBackground,
      appBarTheme: const AppBarTheme(
        backgroundColor: AppColors.lightSurface,
        foregroundColor: AppColors.lightOnBackground,
        elevation: 0,
        centerTitle: false,
      ),
      cardTheme: CardThemeData(
        color: AppColors.lightSurface,
        elevation: 1,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        margin: EdgeInsets.zero,
        shadowColor: AppColors.accent.withAlpha(30),
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: AppColors.accent,
        foregroundColor: Colors.white,
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.lightCard,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: AppColors.lightDivider),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: AppColors.lightDivider),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: AppColors.accent, width: 2),
        ),
        labelStyle: const TextStyle(color: AppColors.lightSubtext),
        hintStyle: const TextStyle(color: AppColors.lightSubtext),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.accent,
          foregroundColor: Colors.white,
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: AppColors.accent,
        ),
      ),
      dividerTheme: const DividerThemeData(
        color: AppColors.lightDivider,
        thickness: 1,
      ),
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: AppColors.lightSurface,
        selectedItemColor: AppColors.accent,
        unselectedItemColor: AppColors.lightSubtext,
        type: BottomNavigationBarType.fixed,
        elevation: 8,
      ),
      chipTheme: ChipThemeData(
        backgroundColor: AppColors.lightCard,
        selectedColor: AppColors.accent,
        labelStyle: const TextStyle(color: AppColors.lightOnSurface),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      textTheme: _buildTextTheme(base.textTheme, AppColors.lightOnBackground, fontScaleFactor),
      iconTheme: const IconThemeData(color: AppColors.lightOnSurface),
      switchTheme: SwitchThemeData(
        thumbColor: WidgetStateProperty.resolveWith((states) =>
            states.contains(WidgetState.selected)
                ? AppColors.accent
                : Colors.grey),
        trackColor: WidgetStateProperty.resolveWith((states) =>
            states.contains(WidgetState.selected)
                ? AppColors.accentLight
                : Colors.grey.shade300),
      ),
    );
  }

  static TextTheme _buildTextTheme(
      TextTheme base, Color color, double scale) {
    return base.copyWith(
      displayLarge: base.displayLarge?.copyWith(color: color, fontSize: (base.displayLarge?.fontSize ?? 57) * scale),
      displayMedium: base.displayMedium?.copyWith(color: color, fontSize: (base.displayMedium?.fontSize ?? 45) * scale),
      displaySmall: base.displaySmall?.copyWith(color: color, fontSize: (base.displaySmall?.fontSize ?? 36) * scale),
      headlineLarge: base.headlineLarge?.copyWith(color: color, fontSize: (base.headlineLarge?.fontSize ?? 32) * scale),
      headlineMedium: base.headlineMedium?.copyWith(color: color, fontSize: (base.headlineMedium?.fontSize ?? 28) * scale),
      headlineSmall: base.headlineSmall?.copyWith(color: color, fontSize: (base.headlineSmall?.fontSize ?? 24) * scale),
      titleLarge: base.titleLarge?.copyWith(color: color, fontSize: (base.titleLarge?.fontSize ?? 22) * scale),
      titleMedium: base.titleMedium?.copyWith(color: color, fontSize: (base.titleMedium?.fontSize ?? 16) * scale),
      titleSmall: base.titleSmall?.copyWith(color: color, fontSize: (base.titleSmall?.fontSize ?? 14) * scale),
      bodyLarge: base.bodyLarge?.copyWith(color: color, fontSize: (base.bodyLarge?.fontSize ?? 16) * scale),
      bodyMedium: base.bodyMedium?.copyWith(color: color, fontSize: (base.bodyMedium?.fontSize ?? 14) * scale),
      bodySmall: base.bodySmall?.copyWith(color: color, fontSize: (base.bodySmall?.fontSize ?? 12) * scale),
      labelLarge: base.labelLarge?.copyWith(color: color, fontSize: (base.labelLarge?.fontSize ?? 14) * scale),
      labelMedium: base.labelMedium?.copyWith(color: color, fontSize: (base.labelMedium?.fontSize ?? 12) * scale),
      labelSmall: base.labelSmall?.copyWith(color: color, fontSize: (base.labelSmall?.fontSize ?? 11) * scale),
    );
  }
}

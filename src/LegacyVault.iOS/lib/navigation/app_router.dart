import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../features/backup/backup_screen.dart';
import '../features/categories/categories_screen.dart';
import '../features/page_detail/page_detail_screen.dart';
import '../features/pages/page_list_screen.dart';
import '../features/reminders/reminders_screen.dart';
import '../features/search/search_screen.dart';
import '../features/settings/settings_screen.dart';
import '../features/shopping/shopping_screen.dart';
import '../features/splash/splash_screen.dart';
import '../features/vault_unlock/vault_unlock_screen.dart';
import '../shared/widgets/bottom_nav_bar.dart';

final GlobalKey<NavigatorState> _rootNavigatorKey =
    GlobalKey<NavigatorState>(debugLabel: 'root');
final GlobalKey<NavigatorState> _shellNavigatorKey =
    GlobalKey<NavigatorState>(debugLabel: 'shell');

final appRouter = GoRouter(
  navigatorKey: _rootNavigatorKey,
  initialLocation: '/',
  routes: [
    GoRoute(
      path: '/',
      builder: (context, state) => const SplashScreen(),
    ),
    ShellRoute(
      navigatorKey: _shellNavigatorKey,
      builder: (context, state, child) => MainShell(child: child),
      routes: [
        GoRoute(
          path: '/vaults',
          pageBuilder: (context, state) => NoTransitionPage(
            key: state.pageKey,
            child: const CategoriesScreen(),
          ),
        ),
        GoRoute(
          path: '/search',
          pageBuilder: (context, state) => NoTransitionPage(
            key: state.pageKey,
            child: const SearchScreen(),
          ),
        ),
        GoRoute(
          path: '/reminders',
          pageBuilder: (context, state) => NoTransitionPage(
            key: state.pageKey,
            child: const RemindersScreen(),
          ),
        ),
        GoRoute(
          path: '/shopping',
          pageBuilder: (context, state) => NoTransitionPage(
            key: state.pageKey,
            child: const ShoppingScreen(),
          ),
        ),
      ],
    ),
    GoRoute(
      path: '/vaults/:categoryId/unlock',
      builder: (context, state) => VaultUnlockScreen(
        categoryId: state.pathParameters['categoryId']!,
      ),
    ),
    GoRoute(
      path: '/vaults/:categoryId/pages',
      builder: (context, state) => PageListScreen(
        categoryId: state.pathParameters['categoryId']!,
      ),
    ),
    GoRoute(
      path: '/vaults/:categoryId/pages/new',
      builder: (context, state) => PageDetailScreen(
        categoryId: state.pathParameters['categoryId']!,
        pageId: null,
        pageType: state.uri.queryParameters['pageType'],
      ),
    ),
    GoRoute(
      path: '/vaults/:categoryId/pages/:pageId',
      builder: (context, state) => PageDetailScreen(
        categoryId: state.pathParameters['categoryId']!,
        pageId: state.pathParameters['pageId'],
        pageType: null,
      ),
    ),
    GoRoute(
      path: '/settings',
      builder: (context, state) => const SettingsScreen(),
    ),
    GoRoute(
      path: '/backup',
      builder: (context, state) => const BackupScreen(),
    ),
  ],
);

import 'package:flutter/material.dart';

enum PageType {
  note,
  password,
  recipe,
  quote,
  homeInventory,
  reminder,
  shoppingList,
}

extension PageTypeExtension on PageType {
  String get label {
    switch (this) {
      case PageType.note:
        return 'Note';
      case PageType.password:
        return 'Password';
      case PageType.recipe:
        return 'Recipe';
      case PageType.quote:
        return 'Quote';
      case PageType.homeInventory:
        return 'Home Inventory';
      case PageType.reminder:
        return 'Reminder';
      case PageType.shoppingList:
        return 'Shopping List';
    }
  }

  String get emoji {
    switch (this) {
      case PageType.note:
        return '📝';
      case PageType.password:
        return '🔑';
      case PageType.recipe:
        return '🍳';
      case PageType.quote:
        return '💬';
      case PageType.homeInventory:
        return '🏠';
      case PageType.reminder:
        return '⏰';
      case PageType.shoppingList:
        return '🛒';
    }
  }

  IconData get icon {
    switch (this) {
      case PageType.note:
        return Icons.note_alt_outlined;
      case PageType.password:
        return Icons.lock_outline;
      case PageType.recipe:
        return Icons.restaurant_outlined;
      case PageType.quote:
        return Icons.format_quote_outlined;
      case PageType.homeInventory:
        return Icons.home_outlined;
      case PageType.reminder:
        return Icons.alarm_outlined;
      case PageType.shoppingList:
        return Icons.shopping_cart_outlined;
    }
  }

  String get dbValue {
    switch (this) {
      case PageType.note:
        return 'NOTE';
      case PageType.password:
        return 'PASSWORD';
      case PageType.recipe:
        return 'RECIPE';
      case PageType.quote:
        return 'QUOTE';
      case PageType.homeInventory:
        return 'HOME_INVENTORY';
      case PageType.reminder:
        return 'REMINDER';
      case PageType.shoppingList:
        return 'SHOPPING_LIST';
    }
  }

  static PageType fromDbValue(String value) {
    switch (value.toUpperCase()) {
      case 'NOTE':
        return PageType.note;
      case 'PASSWORD':
        return PageType.password;
      case 'RECIPE':
        return PageType.recipe;
      case 'QUOTE':
        return PageType.quote;
      case 'HOME_INVENTORY':
        return PageType.homeInventory;
      case 'REMINDER':
        return PageType.reminder;
      case 'SHOPPING_LIST':
        return PageType.shoppingList;
      default:
        return PageType.note;
    }
  }
}

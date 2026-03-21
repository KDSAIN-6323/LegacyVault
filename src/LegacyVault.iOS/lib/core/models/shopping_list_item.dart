import 'dart:convert';

class ShoppingListItem {
  final String id;
  final String name;
  final String quantity;
  final bool checked;

  const ShoppingListItem({
    required this.id,
    required this.name,
    this.quantity = '',
    this.checked = false,
  });

  ShoppingListItem copyWith({
    String? id,
    String? name,
    String? quantity,
    bool? checked,
  }) {
    return ShoppingListItem(
      id: id ?? this.id,
      name: name ?? this.name,
      quantity: quantity ?? this.quantity,
      checked: checked ?? this.checked,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'quantity': quantity,
        'checked': checked,
      };

  factory ShoppingListItem.fromJson(Map<String, dynamic> json) {
    return ShoppingListItem(
      id: json['id'] as String? ?? '',
      name: json['name'] as String? ?? '',
      quantity: json['quantity'] as String? ?? '',
      checked: json['checked'] as bool? ?? false,
    );
  }

  static List<ShoppingListItem> listFromJson(String jsonStr) {
    if (jsonStr.isEmpty) return [];
    try {
      final List<dynamic> list = jsonDecode(jsonStr) as List<dynamic>;
      return list
          .map((e) => ShoppingListItem.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (_) {
      return [];
    }
  }

  static String listToJson(List<ShoppingListItem> items) {
    return jsonEncode(items.map((e) => e.toJson()).toList());
  }
}

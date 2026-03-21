import 'dart:convert';
import 'shopping_list_item.dart';

sealed class PageContent {
  const PageContent();

  Map<String, dynamic> toJson();

  String toJsonString() => jsonEncode(toJson());

  static PageContent fromJson(String type, String jsonStr) {
    final Map<String, dynamic> map =
        jsonDecode(jsonStr) as Map<String, dynamic>;
    switch (type.toUpperCase()) {
      case 'NOTE':
        return NoteContent.fromMap(map);
      case 'PASSWORD':
        return PasswordContent.fromMap(map);
      case 'RECIPE':
        return RecipeContent.fromMap(map);
      case 'QUOTE':
        return QuoteContent.fromMap(map);
      case 'HOME_INVENTORY':
        return HomeInventoryContent.fromMap(map);
      case 'REMINDER':
        return ReminderContent.fromMap(map);
      case 'SHOPPING_LIST':
        return ShoppingListContent.fromMap(map);
      default:
        return NoteContent(body: jsonStr);
    }
  }
}

class NoteContent extends PageContent {
  final String body;

  const NoteContent({required this.body});

  @override
  Map<String, dynamic> toJson() => {'body': body};

  factory NoteContent.fromMap(Map<String, dynamic> map) =>
      NoteContent(body: map['body'] as String? ?? '');

  NoteContent copyWith({String? body}) =>
      NoteContent(body: body ?? this.body);
}

class PasswordContent extends PageContent {
  final String url;
  final String username;
  final String password;
  final String notes;
  final String? totp;

  const PasswordContent({
    required this.url,
    required this.username,
    required this.password,
    required this.notes,
    this.totp,
  });

  @override
  Map<String, dynamic> toJson() => {
        'url': url,
        'username': username,
        'password': password,
        'notes': notes,
        if (totp != null) 'totp': totp,
      };

  factory PasswordContent.fromMap(Map<String, dynamic> map) => PasswordContent(
        url: map['url'] as String? ?? '',
        username: map['username'] as String? ?? '',
        password: map['password'] as String? ?? '',
        notes: map['notes'] as String? ?? '',
        totp: map['totp'] as String?,
      );

  PasswordContent copyWith({
    String? url,
    String? username,
    String? password,
    String? notes,
    String? totp,
  }) =>
      PasswordContent(
        url: url ?? this.url,
        username: username ?? this.username,
        password: password ?? this.password,
        notes: notes ?? this.notes,
        totp: totp ?? this.totp,
      );
}

class RecipeContent extends PageContent {
  final List<String> ingredients;
  final List<String> instructions;
  final int servings;
  final String prepTime;
  final String cookTime;
  final String notes;

  const RecipeContent({
    required this.ingredients,
    required this.instructions,
    required this.servings,
    required this.prepTime,
    required this.cookTime,
    required this.notes,
  });

  @override
  Map<String, dynamic> toJson() => {
        'ingredients': ingredients,
        'instructions': instructions,
        'servings': servings,
        'prepTime': prepTime,
        'cookTime': cookTime,
        'notes': notes,
      };

  factory RecipeContent.fromMap(Map<String, dynamic> map) => RecipeContent(
        ingredients: (map['ingredients'] as List<dynamic>?)
                ?.map((e) => e as String)
                .toList() ??
            [],
        instructions: (map['instructions'] as List<dynamic>?)
                ?.map((e) => e as String)
                .toList() ??
            [],
        servings: map['servings'] as int? ?? 1,
        prepTime: map['prepTime'] as String? ?? '',
        cookTime: map['cookTime'] as String? ?? '',
        notes: map['notes'] as String? ?? '',
      );

  RecipeContent copyWith({
    List<String>? ingredients,
    List<String>? instructions,
    int? servings,
    String? prepTime,
    String? cookTime,
    String? notes,
  }) =>
      RecipeContent(
        ingredients: ingredients ?? this.ingredients,
        instructions: instructions ?? this.instructions,
        servings: servings ?? this.servings,
        prepTime: prepTime ?? this.prepTime,
        cookTime: cookTime ?? this.cookTime,
        notes: notes ?? this.notes,
      );
}

class QuoteContent extends PageContent {
  final String text;
  final String author;
  final String source;
  final List<String> tags;

  const QuoteContent({
    required this.text,
    required this.author,
    required this.source,
    required this.tags,
  });

  @override
  Map<String, dynamic> toJson() => {
        'text': text,
        'author': author,
        'source': source,
        'tags': tags,
      };

  factory QuoteContent.fromMap(Map<String, dynamic> map) => QuoteContent(
        text: map['text'] as String? ?? '',
        author: map['author'] as String? ?? '',
        source: map['source'] as String? ?? '',
        tags: (map['tags'] as List<dynamic>?)?.map((e) => e as String).toList() ??
            [],
      );

  String get formattedForClipboard {
    final buf = StringBuffer('"$text"');
    if (author.isNotEmpty) {
      buf.write(' — $author');
      if (source.isNotEmpty) buf.write(' ($source)');
    }
    return buf.toString();
  }

  QuoteContent copyWith({
    String? text,
    String? author,
    String? source,
    List<String>? tags,
  }) =>
      QuoteContent(
        text: text ?? this.text,
        author: author ?? this.author,
        source: source ?? this.source,
        tags: tags ?? this.tags,
      );
}

class HomeInventoryContent extends PageContent {
  final String itemName;
  final String description;
  final String location;
  final double value;
  final String purchaseDate;
  final String serialNumber;
  final String warrantyExpiry;

  const HomeInventoryContent({
    required this.itemName,
    required this.description,
    required this.location,
    required this.value,
    required this.purchaseDate,
    required this.serialNumber,
    required this.warrantyExpiry,
  });

  @override
  Map<String, dynamic> toJson() => {
        'itemName': itemName,
        'description': description,
        'location': location,
        'value': value,
        'purchaseDate': purchaseDate,
        'serialNumber': serialNumber,
        'warrantyExpiry': warrantyExpiry,
      };

  factory HomeInventoryContent.fromMap(Map<String, dynamic> map) =>
      HomeInventoryContent(
        itemName: map['itemName'] as String? ?? '',
        description: map['description'] as String? ?? '',
        location: map['location'] as String? ?? '',
        value: (map['value'] as num?)?.toDouble() ?? 0.0,
        purchaseDate: map['purchaseDate'] as String? ?? '',
        serialNumber: map['serialNumber'] as String? ?? '',
        warrantyExpiry: map['warrantyExpiry'] as String? ?? '',
      );

  HomeInventoryContent copyWith({
    String? itemName,
    String? description,
    String? location,
    double? value,
    String? purchaseDate,
    String? serialNumber,
    String? warrantyExpiry,
  }) =>
      HomeInventoryContent(
        itemName: itemName ?? this.itemName,
        description: description ?? this.description,
        location: location ?? this.location,
        value: value ?? this.value,
        purchaseDate: purchaseDate ?? this.purchaseDate,
        serialNumber: serialNumber ?? this.serialNumber,
        warrantyExpiry: warrantyExpiry ?? this.warrantyExpiry,
      );
}

class ReminderContent extends PageContent {
  final String date;
  final String? endDate;
  final String tag;
  final String recurrence;
  final int recurrenceInterval;
  final String notes;
  final bool notifyEnabled;
  final int notifyBefore;
  final String notifyUnit;

  const ReminderContent({
    required this.date,
    this.endDate,
    required this.tag,
    required this.recurrence,
    required this.recurrenceInterval,
    required this.notes,
    required this.notifyEnabled,
    required this.notifyBefore,
    required this.notifyUnit,
  });

  @override
  Map<String, dynamic> toJson() => {
        'date': date,
        if (endDate != null) 'endDate': endDate,
        'tag': tag,
        'recurrence': recurrence,
        'recurrenceInterval': recurrenceInterval,
        'notes': notes,
        'notifyEnabled': notifyEnabled,
        'notifyBefore': notifyBefore,
        'notifyUnit': notifyUnit,
      };

  factory ReminderContent.fromMap(Map<String, dynamic> map) => ReminderContent(
        date: map['date'] as String? ?? '',
        endDate: map['endDate'] as String?,
        tag: map['tag'] as String? ?? '',
        recurrence: map['recurrence'] as String? ?? 'NONE',
        recurrenceInterval: map['recurrenceInterval'] as int? ?? 1,
        notes: map['notes'] as String? ?? '',
        notifyEnabled: map['notifyEnabled'] as bool? ?? false,
        notifyBefore: map['notifyBefore'] as int? ?? 0,
        notifyUnit: map['notifyUnit'] as String? ?? 'MINUTES',
      );

  ReminderContent copyWith({
    String? date,
    String? endDate,
    String? tag,
    String? recurrence,
    int? recurrenceInterval,
    String? notes,
    bool? notifyEnabled,
    int? notifyBefore,
    String? notifyUnit,
  }) =>
      ReminderContent(
        date: date ?? this.date,
        endDate: endDate ?? this.endDate,
        tag: tag ?? this.tag,
        recurrence: recurrence ?? this.recurrence,
        recurrenceInterval: recurrenceInterval ?? this.recurrenceInterval,
        notes: notes ?? this.notes,
        notifyEnabled: notifyEnabled ?? this.notifyEnabled,
        notifyBefore: notifyBefore ?? this.notifyBefore,
        notifyUnit: notifyUnit ?? this.notifyUnit,
      );
}

class ShoppingListContent extends PageContent {
  final List<ShoppingListItem> items;
  final String notes;

  const ShoppingListContent({required this.items, required this.notes});

  @override
  Map<String, dynamic> toJson() => {
        'items': items.map((e) => e.toJson()).toList(),
        'notes': notes,
      };

  factory ShoppingListContent.fromMap(Map<String, dynamic> map) =>
      ShoppingListContent(
        items: (map['items'] as List<dynamic>?)
                ?.map((e) =>
                    ShoppingListItem.fromJson(e as Map<String, dynamic>))
                .toList() ??
            [],
        notes: map['notes'] as String? ?? '',
      );

  ShoppingListContent copyWith({
    List<ShoppingListItem>? items,
    String? notes,
  }) =>
      ShoppingListContent(
        items: items ?? this.items,
        notes: notes ?? this.notes,
      );
}

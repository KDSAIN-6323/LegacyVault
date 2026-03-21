import 'page_type.dart';

class PageModel {
  final String id;
  final String categoryId;
  final PageType type;
  final String title;
  final String content;
  final bool isEncrypted;
  final String? encryptionIv;
  final bool isFavorite;
  final int sortOrder;
  final String createdAt;
  final String updatedAt;

  const PageModel({
    required this.id,
    required this.categoryId,
    required this.type,
    required this.title,
    required this.content,
    required this.isEncrypted,
    this.encryptionIv,
    required this.isFavorite,
    required this.sortOrder,
    required this.createdAt,
    required this.updatedAt,
  });

  PageModel copyWith({
    String? id,
    String? categoryId,
    PageType? type,
    String? title,
    String? content,
    bool? isEncrypted,
    String? encryptionIv,
    bool? isFavorite,
    int? sortOrder,
    String? createdAt,
    String? updatedAt,
  }) {
    return PageModel(
      id: id ?? this.id,
      categoryId: categoryId ?? this.categoryId,
      type: type ?? this.type,
      title: title ?? this.title,
      content: content ?? this.content,
      isEncrypted: isEncrypted ?? this.isEncrypted,
      encryptionIv: encryptionIv ?? this.encryptionIv,
      isFavorite: isFavorite ?? this.isFavorite,
      sortOrder: sortOrder ?? this.sortOrder,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  Map<String, dynamic> toMap() => {
        'id': id,
        'category_id': categoryId,
        'type': type.dbValue,
        'title': title,
        'content': content,
        'is_encrypted': isEncrypted ? 1 : 0,
        'encryption_iv': encryptionIv,
        'is_favorite': isFavorite ? 1 : 0,
        'sort_order': sortOrder,
        'created_at': createdAt,
        'updated_at': updatedAt,
      };

  factory PageModel.fromMap(Map<String, dynamic> map) => PageModel(
        id: map['id'] as String,
        categoryId: map['category_id'] as String,
        type: PageTypeExtension.fromDbValue(map['type'] as String? ?? 'NOTE'),
        title: map['title'] as String? ?? '',
        content: map['content'] as String? ?? '',
        isEncrypted: (map['is_encrypted'] as int? ?? 0) == 1,
        encryptionIv: map['encryption_iv'] as String?,
        isFavorite: (map['is_favorite'] as int? ?? 0) == 1,
        sortOrder: map['sort_order'] as int? ?? 0,
        createdAt: map['created_at'] as String? ?? DateTime.now().toIso8601String(),
        updatedAt: map['updated_at'] as String? ?? DateTime.now().toIso8601String(),
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'categoryId': categoryId,
        'type': type.dbValue,
        'title': title,
        'content': content,
        'isEncrypted': isEncrypted,
        'encryptionIv': encryptionIv,
        'isFavorite': isFavorite,
        'sortOrder': sortOrder,
        'createdAt': createdAt,
        'updatedAt': updatedAt,
      };

  factory PageModel.fromJson(Map<String, dynamic> json) => PageModel(
        id: json['id'] as String,
        categoryId: json['categoryId'] as String,
        type: PageTypeExtension.fromDbValue(json['type'] as String? ?? 'NOTE'),
        title: json['title'] as String? ?? '',
        content: json['content'] as String? ?? '',
        isEncrypted: json['isEncrypted'] as bool? ?? false,
        encryptionIv: json['encryptionIv'] as String?,
        isFavorite: json['isFavorite'] as bool? ?? false,
        sortOrder: json['sortOrder'] as int? ?? 0,
        createdAt: json['createdAt'] as String? ?? DateTime.now().toIso8601String(),
        updatedAt: json['updatedAt'] as String? ?? DateTime.now().toIso8601String(),
      );
}

class PageSummary {
  final String id;
  final String categoryId;
  final PageType type;
  final String title;
  final bool isEncrypted;
  final bool isFavorite;
  final int sortOrder;
  final String updatedAt;

  const PageSummary({
    required this.id,
    required this.categoryId,
    required this.type,
    required this.title,
    required this.isEncrypted,
    required this.isFavorite,
    required this.sortOrder,
    required this.updatedAt,
  });

  factory PageSummary.fromPageModel(PageModel page) => PageSummary(
        id: page.id,
        categoryId: page.categoryId,
        type: page.type,
        title: page.title,
        isEncrypted: page.isEncrypted,
        isFavorite: page.isFavorite,
        sortOrder: page.sortOrder,
        updatedAt: page.updatedAt,
      );

  factory PageSummary.fromMap(Map<String, dynamic> map) => PageSummary(
        id: map['id'] as String,
        categoryId: map['category_id'] as String,
        type: PageTypeExtension.fromDbValue(map['type'] as String? ?? 'NOTE'),
        title: map['title'] as String? ?? '',
        isEncrypted: (map['is_encrypted'] as int? ?? 0) == 1,
        isFavorite: (map['is_favorite'] as int? ?? 0) == 1,
        sortOrder: map['sort_order'] as int? ?? 0,
        updatedAt: map['updated_at'] as String? ?? DateTime.now().toIso8601String(),
      );
}

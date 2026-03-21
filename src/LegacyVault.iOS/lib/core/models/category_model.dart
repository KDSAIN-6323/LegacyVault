class Category {
  final String id;
  final String name;
  final String icon;
  final String type;
  final bool isEncrypted;
  final String? encryptionSalt;
  final String? passwordHint;
  final bool isFavorite;
  final int pageCount;
  final String createdAt;
  final String updatedAt;

  const Category({
    required this.id,
    required this.name,
    required this.icon,
    required this.type,
    required this.isEncrypted,
    this.encryptionSalt,
    this.passwordHint,
    required this.isFavorite,
    required this.pageCount,
    required this.createdAt,
    required this.updatedAt,
  });

  Category copyWith({
    String? id,
    String? name,
    String? icon,
    String? type,
    bool? isEncrypted,
    String? encryptionSalt,
    String? passwordHint,
    bool? isFavorite,
    int? pageCount,
    String? createdAt,
    String? updatedAt,
  }) {
    return Category(
      id: id ?? this.id,
      name: name ?? this.name,
      icon: icon ?? this.icon,
      type: type ?? this.type,
      isEncrypted: isEncrypted ?? this.isEncrypted,
      encryptionSalt: encryptionSalt ?? this.encryptionSalt,
      passwordHint: passwordHint ?? this.passwordHint,
      isFavorite: isFavorite ?? this.isFavorite,
      pageCount: pageCount ?? this.pageCount,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  Map<String, dynamic> toMap() => {
        'id': id,
        'name': name,
        'icon': icon,
        'type': type,
        'is_encrypted': isEncrypted ? 1 : 0,
        'encryption_salt': encryptionSalt,
        'password_hint': passwordHint,
        'is_favorite': isFavorite ? 1 : 0,
        'page_count': pageCount,
        'created_at': createdAt,
        'updated_at': updatedAt,
      };

  factory Category.fromMap(Map<String, dynamic> map) => Category(
        id: map['id'] as String,
        name: map['name'] as String,
        icon: map['icon'] as String? ?? '📁',
        type: map['type'] as String? ?? 'GENERAL',
        isEncrypted: (map['is_encrypted'] as int? ?? 0) == 1,
        encryptionSalt: map['encryption_salt'] as String?,
        passwordHint: map['password_hint'] as String?,
        isFavorite: (map['is_favorite'] as int? ?? 0) == 1,
        pageCount: map['page_count'] as int? ?? 0,
        createdAt: map['created_at'] as String? ?? DateTime.now().toIso8601String(),
        updatedAt: map['updated_at'] as String? ?? DateTime.now().toIso8601String(),
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'icon': icon,
        'type': type,
        'isEncrypted': isEncrypted,
        'encryptionSalt': encryptionSalt,
        'passwordHint': passwordHint,
        'isFavorite': isFavorite,
        'pageCount': pageCount,
        'createdAt': createdAt,
        'updatedAt': updatedAt,
      };

  factory Category.fromJson(Map<String, dynamic> json) => Category(
        id: json['id'] as String,
        name: json['name'] as String,
        icon: json['icon'] as String? ?? '📁',
        type: json['type'] as String? ?? 'GENERAL',
        isEncrypted: json['isEncrypted'] as bool? ?? false,
        encryptionSalt: json['encryptionSalt'] as String?,
        passwordHint: json['passwordHint'] as String?,
        isFavorite: json['isFavorite'] as bool? ?? false,
        pageCount: json['pageCount'] as int? ?? 0,
        createdAt: json['createdAt'] as String? ?? DateTime.now().toIso8601String(),
        updatedAt: json['updatedAt'] as String? ?? DateTime.now().toIso8601String(),
      );
}

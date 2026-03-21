import 'dart:convert';
import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:path_provider/path_provider.dart';

import '../database/database_helper.dart';
import '../models/category_model.dart';
import '../models/page_model.dart';

class BackupData {
  final int version;
  final String exportedAt;
  final List<Category> categories;
  final List<PageModel> pages;

  const BackupData({
    required this.version,
    required this.exportedAt,
    required this.categories,
    required this.pages,
  });

  Map<String, dynamic> toJson() => {
        'version': version,
        'exportedAt': exportedAt,
        'categories': categories.map((c) => c.toJson()).toList(),
        'pages': pages.map((p) => p.toJson()).toList(),
      };

  factory BackupData.fromJson(Map<String, dynamic> json) => BackupData(
        version: json['version'] as int? ?? 1,
        exportedAt: json['exportedAt'] as String? ?? '',
        categories: (json['categories'] as List<dynamic>?)
                ?.map((e) => Category.fromJson(e as Map<String, dynamic>))
                .toList() ??
            [],
        pages: (json['pages'] as List<dynamic>?)
                ?.map((e) => PageModel.fromJson(e as Map<String, dynamic>))
                .toList() ??
            [],
      );
}

class BackupRepository {
  final DatabaseHelper _db;

  BackupRepository({DatabaseHelper? db}) : _db = db ?? DatabaseHelper.instance;

  Future<BackupData> createBackup() async {
    final categories = await _db.getAllCategories();
    final pages = await _db.getAllPages();
    return BackupData(
      version: 1,
      exportedAt: DateTime.now().toIso8601String(),
      categories: categories,
      pages: pages,
    );
  }

  Future<String> exportBackupToFile() async {
    final backup = await createBackup();
    final jsonStr = jsonEncode(backup.toJson());

    final dir = await getApplicationDocumentsDirectory();
    final timestamp = DateTime.now()
        .toIso8601String()
        .replaceAll(':', '-')
        .replaceAll('.', '-');
    final file = File('${dir.path}/legacy_vault_backup_$timestamp.json');
    await file.writeAsString(jsonStr);
    return file.path;
  }

  Future<BackupData> importBackupFromFile() async {
    final result = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: ['json'],
    );

    if (result == null || result.files.isEmpty) {
      throw Exception('No file selected');
    }

    final filePath = result.files.first.path;
    if (filePath == null) {
      throw Exception('Could not get file path');
    }

    final file = File(filePath);
    final jsonStr = await file.readAsString();
    final json = jsonDecode(jsonStr) as Map<String, dynamic>;
    return BackupData.fromJson(json);
  }

  Future<void> restoreBackup(BackupData backup) async {
    await _db.importBackup(
      categories: backup.categories,
      pages: backup.pages,
    );
  }
}

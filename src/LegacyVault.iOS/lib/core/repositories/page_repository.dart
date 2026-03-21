import 'package:uuid/uuid.dart';

import '../crypto/crypto_service.dart';
import '../crypto/crypto_service_impl.dart';
import '../crypto/key_cache.dart';
import '../database/database_helper.dart';
import '../models/page_content.dart';
import '../models/page_model.dart';
import '../models/page_type.dart';

class PageRepository {
  final DatabaseHelper _db;
  final CryptoService _crypto;
  final KeyCache _keyCache;
  final Uuid _uuid;

  PageRepository({
    DatabaseHelper? db,
    CryptoService? crypto,
    KeyCache? keyCache,
    Uuid? uuid,
  })  : _db = db ?? DatabaseHelper.instance,
        _crypto = crypto ?? CryptoServiceImpl(),
        _keyCache = keyCache ?? KeyCache.instance,
        _uuid = uuid ?? const Uuid();

  Future<List<PageModel>> getPagesForCategory(String categoryId) =>
      _db.getPagesByCategory(categoryId);

  Future<PageModel?> getPageById(String id) => _db.getPageById(id);

  Future<List<PageModel>> getPagesByType(PageType type) =>
      _db.getPagesByType(type.dbValue);

  Future<List<PageModel>> searchPages(String query) =>
      _db.searchPages(query);

  Future<PageModel> createPage({
    required String categoryId,
    required PageType type,
    required String title,
    required PageContent content,
    bool encryptForVault = false,
  }) async {
    final now = DateTime.now().toIso8601String();
    String contentJson = content.toJsonString();
    bool isEncrypted = false;
    String? encryptionIv;

    if (encryptForVault && _keyCache.has(categoryId)) {
      final key = _keyCache.get(categoryId)!;
      final result = _crypto.encrypt(contentJson, key);
      contentJson = result.ciphertext;
      encryptionIv = result.iv;
      isEncrypted = true;
    }

    final page = PageModel(
      id: _uuid.v4(),
      categoryId: categoryId,
      type: type,
      title: title,
      content: contentJson,
      isEncrypted: isEncrypted,
      encryptionIv: encryptionIv,
      isFavorite: false,
      sortOrder: 0,
      createdAt: now,
      updatedAt: now,
    );
    await _db.insertPage(page);
    return page;
  }

  Future<PageModel> updatePage({
    required PageModel page,
    required String title,
    required PageContent content,
  }) async {
    String contentJson = content.toJsonString();
    bool isEncrypted = false;
    String? encryptionIv;

    if (_keyCache.has(page.categoryId)) {
      final key = _keyCache.get(page.categoryId)!;
      final result = _crypto.encrypt(contentJson, key);
      contentJson = result.ciphertext;
      encryptionIv = result.iv;
      isEncrypted = true;
    }

    final updated = page.copyWith(
      title: title,
      content: contentJson,
      isEncrypted: isEncrypted,
      encryptionIv: encryptionIv,
      updatedAt: DateTime.now().toIso8601String(),
    );
    await _db.updatePage(updated);
    return updated;
  }

  Future<void> deletePage(String id, String categoryId) =>
      _db.deletePage(id, categoryId);

  /// Decrypt and parse content from a page. Returns null if decryption fails.
  PageContent? decryptAndParseContent(PageModel page) {
    try {
      String contentJson = page.content;
      if (page.isEncrypted && page.encryptionIv != null) {
        final key = _keyCache.get(page.categoryId);
        if (key == null) return null;
        contentJson = _crypto.decrypt(page.content, page.encryptionIv!, key);
      }
      return PageContent.fromJson(page.type.dbValue, contentJson);
    } catch (_) {
      return null;
    }
  }

  Future<List<PageModel>> getAllPages() => _db.getAllPages();

  Future<void> toggleFavorite(PageModel page) async {
    await _db.updatePage(
      page.copyWith(
        isFavorite: !page.isFavorite,
        updatedAt: DateTime.now().toIso8601String(),
      ),
    );
  }
}

import 'dart:typed_data';

/// In-memory cache of derived encryption keys, keyed by category ID.
/// Keys are never persisted to disk.
class KeyCache {
  KeyCache._();
  static final KeyCache instance = KeyCache._();

  final Map<String, Uint8List> _cache = {};

  void store(String categoryId, Uint8List key) {
    _cache[categoryId] = key;
  }

  Uint8List? get(String categoryId) => _cache[categoryId];

  bool has(String categoryId) => _cache.containsKey(categoryId);

  void remove(String categoryId) => _cache.remove(categoryId);

  void clearAll() => _cache.clear();
}

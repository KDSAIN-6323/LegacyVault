import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Wraps flutter_secure_storage to persist biometric-verified vault keys.
/// Only stores a key after the user has unlocked via password AND enrolled biometrics.
class VaultKeyStore {
  static const String _prefix = 'vault_key_';

  final FlutterSecureStorage _storage;

  VaultKeyStore({FlutterSecureStorage? storage})
      : _storage = storage ??
            const FlutterSecureStorage(
              iOptions: IOSOptions(
                // first_unlock_this_device prevents keys migrating to a new device
                // via iCloud backup, keeping vault keys device-bound.
                accessibility: KeychainAccessibility.first_unlock_this_device,
              ),
            );

  String _keyFor(String categoryId) => '$_prefix$categoryId';

  Future<void> storeKey(String categoryId, Uint8List key) async {
    await _storage.write(
      key: _keyFor(categoryId),
      value: base64.encode(key),
    );
  }

  Future<Uint8List?> loadKey(String categoryId) async {
    final val = await _storage.read(key: _keyFor(categoryId));
    if (val == null) return null;
    return base64.decode(val);
  }

  Future<bool> hasKey(String categoryId) async {
    return await _storage.containsKey(key: _keyFor(categoryId));
  }

  Future<void> deleteKey(String categoryId) async {
    await _storage.delete(key: _keyFor(categoryId));
  }

  Future<void> deleteAllKeys() async {
    final all = await _storage.readAll();
    for (final k in all.keys) {
      if (k.startsWith(_prefix)) {
        await _storage.delete(key: k);
      }
    }
  }
}

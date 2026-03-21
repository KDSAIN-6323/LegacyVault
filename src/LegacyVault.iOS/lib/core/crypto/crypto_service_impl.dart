import 'dart:convert';
import 'dart:math';
import 'dart:typed_data';

import 'package:pointycastle/export.dart';

import 'crypto_service.dart';

class CryptoServiceImpl implements CryptoService {
  static const int _iterations = 310000;
  static const int _keyLength = 32;
  static const int _saltLength = 32;
  static const int _ivLength = 12;
  static const int _tagLength = 16;

  final Random _secureRandom = Random.secure();

  Uint8List _randomBytes(int length) {
    final bytes = Uint8List(length);
    for (int i = 0; i < length; i++) {
      bytes[i] = _secureRandom.nextInt(256);
    }
    return bytes;
  }

  @override
  String generateSalt() {
    return base64.encode(_randomBytes(_saltLength));
  }

  @override
  Uint8List deriveKey(String password, String saltBase64) {
    final salt = base64.decode(saltBase64);
    final passwordBytes = Uint8List.fromList(utf8.encode(password));

    final params = Pbkdf2Parameters(
      salt,
      _iterations,
      _keyLength,
    );

    final keyDerivator = PBKDF2KeyDerivator(HMac(SHA256Digest(), 64));
    keyDerivator.init(params);

    final key = Uint8List(_keyLength);
    keyDerivator.deriveKey(passwordBytes, 0, key, 0);
    return key;
  }

  @override
  ({String ciphertext, String iv}) encrypt(String plaintext, Uint8List key) {
    final iv = _randomBytes(_ivLength);
    final plaintextBytes = Uint8List.fromList(utf8.encode(plaintext));

    final params = AEADParameters(
      KeyParameter(key),
      _tagLength * 8,
      iv,
      Uint8List(0),
    );

    final cipher = GCMBlockCipher(AESEngine())..init(true, params);

    final outputLength = cipher.getOutputSize(plaintextBytes.length);
    final output = Uint8List(outputLength);
    int offset = cipher.processBytes(plaintextBytes, 0, plaintextBytes.length, output, 0);
    offset += cipher.doFinal(output, offset);

    // output from pointycastle GCM is [ciphertext | tag]
    // We need to store as [tag | ciphertext] (mobile wire format)
    final ciphertextOnly = output.sublist(0, output.length - _tagLength);
    final tag = output.sublist(output.length - _tagLength);

    final wireBytes = Uint8List(_tagLength + ciphertextOnly.length);
    wireBytes.setRange(0, _tagLength, tag);
    wireBytes.setRange(_tagLength, wireBytes.length, ciphertextOnly);

    return (
      ciphertext: base64.encode(wireBytes),
      iv: base64.encode(iv),
    );
  }

  @override
  String decrypt(String ciphertextBase64, String ivBase64, Uint8List key) {
    final iv = base64.decode(ivBase64);
    final wireBytes = base64.decode(ciphertextBase64);

    // Try mobile format first: [16-byte tag][ciphertext]
    try {
      return _decryptWithTagPrepended(wireBytes, iv, key);
    } catch (_) {
      // Fall back to web format: [ciphertext][16-byte tag]
      return _decryptWithTagAppended(wireBytes, iv, key);
    }
  }

  String _decryptWithTagPrepended(
      Uint8List wireBytes, Uint8List iv, Uint8List key) {
    if (wireBytes.length < _tagLength) {
      throw ArgumentError('Ciphertext too short for mobile format');
    }
    final tag = wireBytes.sublist(0, _tagLength);
    final ciphertextOnly = wireBytes.sublist(_tagLength);

    // pointycastle GCM expects [ciphertext | tag]
    final combined = Uint8List(ciphertextOnly.length + _tagLength);
    combined.setRange(0, ciphertextOnly.length, ciphertextOnly);
    combined.setRange(ciphertextOnly.length, combined.length, tag);

    return _gcmDecrypt(combined, iv, key);
  }

  String _decryptWithTagAppended(
      Uint8List wireBytes, Uint8List iv, Uint8List key) {
    if (wireBytes.length < _tagLength) {
      throw ArgumentError('Ciphertext too short for web format');
    }
    // Already in [ciphertext | tag] order — pass directly
    return _gcmDecrypt(wireBytes, iv, key);
  }

  String _gcmDecrypt(Uint8List combined, Uint8List iv, Uint8List key) {
    final params = AEADParameters(
      KeyParameter(key),
      _tagLength * 8,
      iv,
      Uint8List(0),
    );

    final cipher = GCMBlockCipher(AESEngine())..init(false, params);

    final outputLength = cipher.getOutputSize(combined.length);
    final output = Uint8List(outputLength);
    int offset = cipher.processBytes(combined, 0, combined.length, output, 0);
    offset += cipher.doFinal(output, offset);

    return utf8.decode(output.sublist(0, offset));
  }
}

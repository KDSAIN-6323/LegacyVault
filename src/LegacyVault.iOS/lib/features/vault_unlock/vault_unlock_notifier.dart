import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:local_auth/local_auth.dart';

import '../../core/crypto/crypto_service_impl.dart';
import '../../core/crypto/key_cache.dart';
import '../../core/repositories/category_repository.dart';
import '../../core/secure_storage/vault_key_store.dart';
import '../categories/categories_notifier.dart';

enum UnlockStatus { idle, loading, success, error, biometricAvailable }

class VaultUnlockState {
  final UnlockStatus status;
  final String? errorMessage;
  final bool canUseBiometrics;
  final bool hasBiometricEnrolled;

  const VaultUnlockState({
    this.status = UnlockStatus.idle,
    this.errorMessage,
    this.canUseBiometrics = false,
    this.hasBiometricEnrolled = false,
  });

  VaultUnlockState copyWith({
    UnlockStatus? status,
    String? errorMessage,
    bool? canUseBiometrics,
    bool? hasBiometricEnrolled,
  }) =>
      VaultUnlockState(
        status: status ?? this.status,
        errorMessage: errorMessage,
        canUseBiometrics: canUseBiometrics ?? this.canUseBiometrics,
        hasBiometricEnrolled: hasBiometricEnrolled ?? this.hasBiometricEnrolled,
      );
}

class VaultUnlockNotifier extends StateNotifier<VaultUnlockState> {
  final CategoryRepository _categoryRepo;
  final VaultKeyStore _keyStore;
  final LocalAuthentication _localAuth;

  VaultUnlockNotifier({
    required CategoryRepository categoryRepo,
    VaultKeyStore? keyStore,
    LocalAuthentication? localAuth,
  })  : _categoryRepo = categoryRepo,
        _keyStore = keyStore ?? VaultKeyStore(),
        _localAuth = localAuth ?? LocalAuthentication(),
        super(const VaultUnlockState()) {
    _checkBiometricsAsync();
  }

  Future<void> _checkBiometricsAsync() async {
    try {
      final canCheck = await _localAuth.canCheckBiometrics;
      final available = await _localAuth.getAvailableBiometrics();
      state = state.copyWith(
        canUseBiometrics: canCheck && available.isNotEmpty,
      );
    } catch (_) {
      // Biometrics not available on this device — leave canUseBiometrics false
      state = state.copyWith(canUseBiometrics: false);
    }
  }

  Future<bool> checkBiometricEnrollmentAsync(String categoryId) async {
    final hasKey = await _keyStore.hasKey(categoryId);
    state = state.copyWith(hasBiometricEnrolled: hasKey);
    return hasKey;
  }

  Future<bool> unlockWithBiometricsAsync(String categoryId) async {
    try {
      final authenticated = await _localAuth.authenticate(
        localizedReason: 'Authenticate to unlock vault',
        options: const AuthenticationOptions(
          biometricOnly: false,
          stickyAuth: true,
        ),
      );
      if (!authenticated) return false;

      final key = await _keyStore.loadKey(categoryId);
      if (key == null) return false;

      KeyCache.instance.store(categoryId, key);
      state = state.copyWith(status: UnlockStatus.success);
      return true;
    } catch (e) {
      state = state.copyWith(
        status: UnlockStatus.error,
        errorMessage: 'Biometric authentication failed',
      );
      return false;
    }
  }

  Future<bool> unlockWithPasswordAsync(
      String categoryId, String password) async {
    state = state.copyWith(status: UnlockStatus.loading);
    try {
      final category = await _categoryRepo.getCategoryById(categoryId);
      if (category == null) {
        state = state.copyWith(
          status: UnlockStatus.error,
          errorMessage: 'Vault not found',
        );
        return false;
      }

      if (category.encryptionSalt == null) {
        state = state.copyWith(
          status: UnlockStatus.error,
          errorMessage: 'Vault has no encryption salt',
        );
        return false;
      }

      final crypto = CryptoServiceImpl();
      final key = crypto.deriveKey(password, category.encryptionSalt!);

      // Store key in memory
      KeyCache.instance.store(categoryId, key);

      state = state.copyWith(status: UnlockStatus.success);
      return true;
    } catch (e) {
      state = state.copyWith(
        status: UnlockStatus.error,
        errorMessage: 'Incorrect password',
      );
      return false;
    }
  }

  Future<void> enrollBiometricsAsync(String categoryId) async {
    if (!state.canUseBiometrics) return;
    final key = KeyCache.instance.get(categoryId);
    if (key == null) return;

    try {
      final authenticated = await _localAuth.authenticate(
        localizedReason: 'Enroll biometrics for this vault',
        options: const AuthenticationOptions(
          biometricOnly: false,
          stickyAuth: true,
        ),
      );
      if (authenticated) {
        await _keyStore.storeKey(categoryId, key);
        state = state.copyWith(hasBiometricEnrolled: true);
      }
    } catch (e) {
      state = state.copyWith(
        status: UnlockStatus.error,
        errorMessage: 'Failed to enroll biometrics',
      );
    }
  }

  void reset() {
    state = const VaultUnlockState();
  }
}

final vaultUnlockNotifierProvider = StateNotifierProvider.autoDispose
    .family<VaultUnlockNotifier, VaultUnlockState, String>(
  (ref, categoryId) => VaultUnlockNotifier(
    categoryRepo: ref.watch(categoryRepositoryProvider),
  ),
);

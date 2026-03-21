import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../theme/app_colors.dart';
import 'vault_unlock_notifier.dart';

class VaultUnlockScreen extends ConsumerStatefulWidget {
  final String categoryId;

  const VaultUnlockScreen({super.key, required this.categoryId});

  @override
  ConsumerState<VaultUnlockScreen> createState() => _VaultUnlockScreenState();
}

class _VaultUnlockScreenState extends ConsumerState<VaultUnlockScreen> {
  final _passwordCtrl = TextEditingController();
  bool _obscure = true;
  bool _enrollBiometricRequested = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      final notifier =
          ref.read(vaultUnlockNotifierProvider(widget.categoryId).notifier);
      final hasEnrolled = await notifier.checkBiometricEnrollmentAsync(widget.categoryId);
      if (hasEnrolled) {
        _tryBiometric();
      }
    });
  }

  @override
  void dispose() {
    _passwordCtrl.dispose();
    super.dispose();
  }

  Future<void> _tryBiometric() async {
    final notifier =
        ref.read(vaultUnlockNotifierProvider(widget.categoryId).notifier);
    final success = await notifier.unlockWithBiometricsAsync(widget.categoryId);
    if (success && mounted) {
      context.pushReplacement('/vaults/${widget.categoryId}/pages');
    }
  }

  Future<void> _unlock() async {
    final password = _passwordCtrl.text;
    if (password.isEmpty) return;

    final notifier =
        ref.read(vaultUnlockNotifierProvider(widget.categoryId).notifier);
    final success =
        await notifier.unlockWithPasswordAsync(widget.categoryId, password);

    if (success && mounted) {
      final state = ref.read(vaultUnlockNotifierProvider(widget.categoryId));
      // Offer biometric enrollment if available
      if (state.canUseBiometrics && !state.hasBiometricEnrolled) {
        _offerBiometricEnrollment(notifier);
      } else {
        context.pushReplacement('/vaults/${widget.categoryId}/pages');
      }
    }
  }

  Future<void> _offerBiometricEnrollment(VaultUnlockNotifier notifier) async {
    if (!mounted) return;
    final enroll = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Enable Biometric Unlock'),
        content: const Text(
            'Would you like to use Face ID / Touch ID to unlock this vault in the future?'),
        actions: [
          TextButton(
            onPressed: () => ctx.pop(false),
            child: const Text('Not Now'),
          ),
          ElevatedButton(
            onPressed: () => ctx.pop(true),
            child: const Text('Enable'),
          ),
        ],
      ),
    );

    if (enroll == true) {
      await notifier.enrollBiometricsAsync(widget.categoryId);
    }
    if (mounted) {
      context.pushReplacement('/vaults/${widget.categoryId}/pages');
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(vaultUnlockNotifierProvider(widget.categoryId));
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Unlock Vault'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(
                Icons.lock_outline,
                size: 72,
                color: AppColors.accentLight,
              ),
              const SizedBox(height: 24),
              Text(
                'Enter Password',
                style: theme.textTheme.headlineSmall
                    ?.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              Text(
                'This vault is encrypted. Enter your password to unlock it.',
                textAlign: TextAlign.center,
                style: theme.textTheme.bodyMedium
                    ?.copyWith(color: AppColors.darkSubtext),
              ),
              const SizedBox(height: 32),
              TextField(
                controller: _passwordCtrl,
                obscureText: _obscure,
                decoration: InputDecoration(
                  labelText: 'Vault Password',
                  prefixIcon: const Icon(Icons.lock_outline),
                  suffixIcon: IconButton(
                    icon:
                        Icon(_obscure ? Icons.visibility_off : Icons.visibility),
                    onPressed: () => setState(() => _obscure = !_obscure),
                  ),
                ),
                onSubmitted: (_) => _unlock(),
              ),
              if (state.errorMessage != null) ...[
                const SizedBox(height: 12),
                Text(
                  state.errorMessage!,
                  style: const TextStyle(color: AppColors.error),
                ),
              ],
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: state.status == UnlockStatus.loading
                      ? null
                      : _unlock,
                  child: state.status == UnlockStatus.loading
                      ? const SizedBox(
                          height: 20,
                          width: 20,
                          child: CircularProgressIndicator(
                              strokeWidth: 2, color: Colors.white),
                        )
                      : const Text('Unlock'),
                ),
              ),
              if (state.canUseBiometrics && state.hasBiometricEnrolled) ...[
                const SizedBox(height: 16),
                TextButton.icon(
                  onPressed: _tryBiometric,
                  icon: const Icon(Icons.fingerprint),
                  label: const Text('Use Biometrics'),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

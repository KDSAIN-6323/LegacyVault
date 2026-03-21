import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/crypto/crypto_service_impl.dart';
import '../../core/models/category_model.dart';
import '../../theme/app_colors.dart';
import 'categories_notifier.dart';

const _icons = [
  '📁', '🏠', '💼', '🎓', '❤️', '🍀', '⭐', '🔒',
  '📚', '💡', '🎯', '🌟', '🛡️', '🔮', '🎨', '🌈',
];

class CategoryCreateEditSheet extends ConsumerStatefulWidget {
  final Category? existing;

  const CategoryCreateEditSheet({super.key, this.existing});

  @override
  ConsumerState<CategoryCreateEditSheet> createState() =>
      _CategoryCreateEditSheetState();
}

class _CategoryCreateEditSheetState
    extends ConsumerState<CategoryCreateEditSheet> {
  late final TextEditingController _nameCtrl;
  late final TextEditingController _passwordCtrl;
  late final TextEditingController _hintCtrl;
  late String _selectedIcon;
  late bool _isEncrypted;
  bool _obscurePassword = true;

  @override
  void initState() {
    super.initState();
    _nameCtrl = TextEditingController(text: widget.existing?.name ?? '');
    _passwordCtrl = TextEditingController();
    _hintCtrl =
        TextEditingController(text: widget.existing?.passwordHint ?? '');
    _selectedIcon = widget.existing?.icon ?? '📁';
    _isEncrypted = widget.existing?.isEncrypted ?? false;
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _passwordCtrl.dispose();
    _hintCtrl.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    final name = _nameCtrl.text.trim();
    if (name.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Name is required')),
      );
      return;
    }

    if (_isEncrypted &&
        widget.existing == null &&
        _passwordCtrl.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Password is required for encrypted vault')),
      );
      return;
    }

    final notifier = ref.read(categoriesNotifierProvider.notifier);

    if (widget.existing == null) {
      // Create
      String? salt;
      if (_isEncrypted && _passwordCtrl.text.isNotEmpty) {
        final crypto = CryptoServiceImpl();
        salt = crypto.generateSalt();
      }
      await notifier.createCategory(
        name: name,
        icon: _selectedIcon,
        isEncrypted: _isEncrypted,
        encryptionSalt: salt,
        passwordHint: _hintCtrl.text.trim().isEmpty ? null : _hintCtrl.text.trim(),
      );
    } else {
      await notifier.updateCategory(
        widget.existing!.copyWith(
          name: name,
          icon: _selectedIcon,
          passwordHint:
              _hintCtrl.text.trim().isEmpty ? null : _hintCtrl.text.trim(),
          updatedAt: DateTime.now().toIso8601String(),
        ),
      );
    }

    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: EdgeInsets.only(
        left: 20,
        right: 20,
        top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 20,
      ),
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              widget.existing == null ? 'New Vault' : 'Edit Vault',
              style: theme.textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 20),
            TextField(
              controller: _nameCtrl,
              decoration: const InputDecoration(labelText: 'Vault Name'),
              textCapitalization: TextCapitalization.words,
            ),
            const SizedBox(height: 16),
            Text('Icon', style: theme.textTheme.labelLarge),
            const SizedBox(height: 8),
            SizedBox(
              height: 80,
              child: GridView.builder(
                scrollDirection: Axis.horizontal,
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  mainAxisSpacing: 8,
                  crossAxisSpacing: 8,
                ),
                itemCount: _icons.length,
                itemBuilder: (context, index) {
                  final icon = _icons[index];
                  final selected = icon == _selectedIcon;
                  return GestureDetector(
                    onTap: () => setState(() => _selectedIcon = icon),
                    child: Container(
                      decoration: BoxDecoration(
                        color: selected
                            ? AppColors.accent.withAlpha(60)
                            : AppColors.darkCard,
                        borderRadius: BorderRadius.circular(8),
                        border: selected
                            ? Border.all(color: AppColors.accent, width: 2)
                            : null,
                      ),
                      child: Center(
                        child: Text(icon, style: const TextStyle(fontSize: 22)),
                      ),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 16),
            if (widget.existing == null) ...[
              SwitchListTile(
                title: const Text('Encrypted Vault'),
                subtitle: const Text('Require password to access'),
                value: _isEncrypted,
                onChanged: (v) => setState(() => _isEncrypted = v),
                contentPadding: EdgeInsets.zero,
              ),
              if (_isEncrypted) ...[
                const SizedBox(height: 8),
                TextField(
                  controller: _passwordCtrl,
                  decoration: InputDecoration(
                    labelText: 'Vault Password',
                    suffixIcon: IconButton(
                      icon: Icon(_obscurePassword
                          ? Icons.visibility_off
                          : Icons.visibility),
                      onPressed: () =>
                          setState(() => _obscurePassword = !_obscurePassword),
                    ),
                  ),
                  obscureText: _obscurePassword,
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: _hintCtrl,
                  decoration:
                      const InputDecoration(labelText: 'Password Hint (optional)'),
                ),
              ],
            ] else if (widget.existing?.isEncrypted == true) ...[
              const SizedBox(height: 8),
              TextField(
                controller: _hintCtrl,
                decoration:
                    const InputDecoration(labelText: 'Password Hint (optional)'),
              ),
            ],
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: _save,
              child: Text(
                widget.existing == null ? 'Create Vault' : 'Save Changes',
              ),
            ),
          ],
        ),
      ),
    );
  }
}

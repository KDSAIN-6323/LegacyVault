import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../core/models/page_content.dart';
import '../../../theme/app_colors.dart';

class PasswordEditor extends StatefulWidget {
  final PasswordContent? initial;
  final ValueChanged<PasswordContent> onChanged;

  const PasswordEditor({super.key, this.initial, required this.onChanged});

  @override
  State<PasswordEditor> createState() => _PasswordEditorState();
}

class _PasswordEditorState extends State<PasswordEditor> {
  late final TextEditingController _urlCtrl;
  late final TextEditingController _usernameCtrl;
  late final TextEditingController _passwordCtrl;
  late final TextEditingController _notesCtrl;
  late final TextEditingController _totpCtrl;
  bool _obscurePassword = true;

  @override
  void initState() {
    super.initState();
    final init = widget.initial;
    _urlCtrl = TextEditingController(text: init?.url ?? '');
    _usernameCtrl = TextEditingController(text: init?.username ?? '');
    _passwordCtrl = TextEditingController(text: init?.password ?? '');
    _notesCtrl = TextEditingController(text: init?.notes ?? '');
    _totpCtrl = TextEditingController(text: init?.totp ?? '');

    for (final ctrl in [_urlCtrl, _usernameCtrl, _passwordCtrl, _notesCtrl, _totpCtrl]) {
      ctrl.addListener(_notify);
    }
    WidgetsBinding.instance.addPostFrameCallback((_) => _notify());
  }

  void _notify() {
    widget.onChanged(PasswordContent(
      url: _urlCtrl.text,
      username: _usernameCtrl.text,
      password: _passwordCtrl.text,
      notes: _notesCtrl.text,
      totp: _totpCtrl.text.isEmpty ? null : _totpCtrl.text,
    ));
  }

  @override
  void dispose() {
    for (final ctrl in [_urlCtrl, _usernameCtrl, _passwordCtrl, _notesCtrl, _totpCtrl]) {
      ctrl.removeListener(_notify);
      ctrl.dispose();
    }
    super.dispose();
  }

  void _copyToClipboard(String text, String label) {
    Clipboard.setData(ClipboardData(text: text));
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('$label copied to clipboard')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TextField(
          controller: _urlCtrl,
          decoration: const InputDecoration(
            labelText: 'URL / Website',
            prefixIcon: Icon(Icons.link),
          ),
          keyboardType: TextInputType.url,
          autocorrect: false,
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _usernameCtrl,
          decoration: InputDecoration(
            labelText: 'Username / Email',
            prefixIcon: const Icon(Icons.person_outline),
            suffixIcon: IconButton(
              icon: const Icon(Icons.copy, size: 18),
              onPressed: () =>
                  _copyToClipboard(_usernameCtrl.text, 'Username'),
            ),
          ),
          autocorrect: false,
          keyboardType: TextInputType.emailAddress,
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _passwordCtrl,
          obscureText: _obscurePassword,
          decoration: InputDecoration(
            labelText: 'Password',
            prefixIcon: const Icon(Icons.lock_outline),
            suffixIcon: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  icon: Icon(
                    _obscurePassword ? Icons.visibility_off : Icons.visibility,
                    size: 20,
                  ),
                  onPressed: () =>
                      setState(() => _obscurePassword = !_obscurePassword),
                ),
                IconButton(
                  icon: const Icon(Icons.copy, size: 18),
                  onPressed: () =>
                      _copyToClipboard(_passwordCtrl.text, 'Password'),
                ),
              ],
            ),
          ),
          autocorrect: false,
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _totpCtrl,
          decoration: const InputDecoration(
            labelText: 'TOTP Secret (optional)',
            prefixIcon: Icon(Icons.security),
          ),
          autocorrect: false,
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _notesCtrl,
          maxLines: 4,
          decoration: const InputDecoration(
            labelText: 'Notes',
            prefixIcon: Icon(Icons.notes),
            alignLabelWithHint: true,
          ),
        ),
        const SizedBox(height: 12),
        OutlinedButton.icon(
          onPressed: _generatePassword,
          icon: const Icon(Icons.auto_fix_high, size: 18),
          label: const Text('Generate Strong Password'),
          style: OutlinedButton.styleFrom(
            foregroundColor: AppColors.accentLight,
            side: const BorderSide(color: AppColors.accentLight),
          ),
        ),
      ],
    );
  }

  void _generatePassword() {
    const chars =
        'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#\$%^&*';
    final rng = Random.secure();
    final buf = StringBuffer();
    for (int i = 0; i < 20; i++) {
      buf.write(chars[rng.nextInt(chars.length)]);
    }
    _passwordCtrl.text = buf.toString();
  }
}

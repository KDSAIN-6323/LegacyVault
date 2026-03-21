import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../core/models/page_content.dart';
import '../../../theme/app_colors.dart';

class QuoteEditor extends StatefulWidget {
  final QuoteContent? initial;
  final ValueChanged<QuoteContent> onChanged;

  const QuoteEditor({super.key, this.initial, required this.onChanged});

  @override
  State<QuoteEditor> createState() => _QuoteEditorState();
}

class _QuoteEditorState extends State<QuoteEditor> {
  late final TextEditingController _textCtrl;
  late final TextEditingController _authorCtrl;
  late final TextEditingController _sourceCtrl;
  late final TextEditingController _tagsCtrl;

  @override
  void initState() {
    super.initState();
    final init = widget.initial;
    _textCtrl = TextEditingController(text: init?.text ?? '');
    _authorCtrl = TextEditingController(text: init?.author ?? '');
    _sourceCtrl = TextEditingController(text: init?.source ?? '');
    _tagsCtrl =
        TextEditingController(text: init?.tags.join(', ') ?? '');

    for (final ctrl in [_textCtrl, _authorCtrl, _sourceCtrl, _tagsCtrl]) {
      ctrl.addListener(_notify);
    }
    WidgetsBinding.instance.addPostFrameCallback((_) => _notify());
  }

  void _notify() {
    final tags = _tagsCtrl.text
        .split(',')
        .map((s) => s.trim())
        .where((s) => s.isNotEmpty)
        .toList();
    widget.onChanged(QuoteContent(
      text: _textCtrl.text,
      author: _authorCtrl.text,
      source: _sourceCtrl.text,
      tags: tags,
    ));
    if (mounted) setState(() {});
  }

  @override
  void dispose() {
    for (final ctrl in [_textCtrl, _authorCtrl, _sourceCtrl, _tagsCtrl]) {
      ctrl.removeListener(_notify);
      ctrl.dispose();
    }
    super.dispose();
  }

  void _copyToClipboard() {
    final tags = _tagsCtrl.text
        .split(',')
        .map((s) => s.trim())
        .where((s) => s.isNotEmpty)
        .toList();
    final content = QuoteContent(
      text: _textCtrl.text,
      author: _authorCtrl.text,
      source: _sourceCtrl.text,
      tags: tags,
    );
    Clipboard.setData(ClipboardData(text: content.formattedForClipboard));
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Quote copied to clipboard')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TextField(
          controller: _textCtrl,
          maxLines: 5,
          minLines: 3,
          decoration: const InputDecoration(
            labelText: 'Quote Text',
            prefixIcon: Icon(Icons.format_quote),
            alignLabelWithHint: true,
          ),
          textCapitalization: TextCapitalization.sentences,
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _authorCtrl,
          decoration: const InputDecoration(
            labelText: 'Author',
            prefixIcon: Icon(Icons.person_outline),
          ),
          textCapitalization: TextCapitalization.words,
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _sourceCtrl,
          decoration: const InputDecoration(
            labelText: 'Source (book, speech, etc.)',
            prefixIcon: Icon(Icons.book_outlined),
          ),
          textCapitalization: TextCapitalization.words,
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _tagsCtrl,
          decoration: const InputDecoration(
            labelText: 'Tags (comma-separated)',
            prefixIcon: Icon(Icons.tag),
          ),
        ),
        const SizedBox(height: 16),
        OutlinedButton.icon(
          onPressed: _textCtrl.text.isNotEmpty ? _copyToClipboard : null,
          icon: const Icon(Icons.copy, size: 18),
          label: const Text('Copy Quote to Clipboard'),
          style: OutlinedButton.styleFrom(
            foregroundColor: AppColors.accentLight,
            side: const BorderSide(color: AppColors.accentLight),
          ),
        ),
        const SizedBox(height: 8),
        if (_textCtrl.text.isNotEmpty)
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: AppColors.darkCard,
              borderRadius: BorderRadius.circular(10),
              border: Border.all(color: AppColors.darkDivider),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Preview',
                  style: Theme.of(context).textTheme.labelSmall?.copyWith(
                        color: AppColors.darkSubtext,
                      ),
                ),
                const SizedBox(height: 6),
                Text(
                  '"${_textCtrl.text}"',
                  style: const TextStyle(
                    fontStyle: FontStyle.italic,
                    color: AppColors.darkOnBackground,
                  ),
                ),
                if (_authorCtrl.text.isNotEmpty) ...[
                  const SizedBox(height: 4),
                  Text(
                    '— ${_authorCtrl.text}${_sourceCtrl.text.isNotEmpty ? ' (${_sourceCtrl.text})' : ''}',
                    style: const TextStyle(
                      color: AppColors.accentLight,
                      fontSize: 13,
                    ),
                  ),
                ],
              ],
            ),
          ),
      ],
    );
  }
}

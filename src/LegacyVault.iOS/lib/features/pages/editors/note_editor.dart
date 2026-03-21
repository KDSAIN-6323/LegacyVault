import 'package:flutter/material.dart';

import '../../../core/models/page_content.dart';

class NoteEditor extends StatefulWidget {
  final NoteContent? initial;
  final ValueChanged<NoteContent> onChanged;

  const NoteEditor({super.key, this.initial, required this.onChanged});

  @override
  State<NoteEditor> createState() => _NoteEditorState();
}

class _NoteEditorState extends State<NoteEditor> {
  late final TextEditingController _bodyCtrl;

  @override
  void initState() {
    super.initState();
    _bodyCtrl = TextEditingController(text: widget.initial?.body ?? '');
    _bodyCtrl.addListener(_notify);
    // Emit initial value
    WidgetsBinding.instance.addPostFrameCallback((_) => _notify());
  }

  void _notify() {
    widget.onChanged(NoteContent(body: _bodyCtrl.text));
  }

  @override
  void dispose() {
    _bodyCtrl.removeListener(_notify);
    _bodyCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: _bodyCtrl,
      maxLines: null,
      minLines: 10,
      decoration: const InputDecoration(
        labelText: 'Note',
        alignLabelWithHint: true,
      ),
      textCapitalization: TextCapitalization.sentences,
    );
  }
}

import 'package:flutter/material.dart';

import '../../../core/models/page_content.dart';

const _recurrenceOptions = ['NONE', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'];
const _notifyUnitOptions = ['MINUTES', 'HOURS', 'DAYS'];

class ReminderEditor extends StatefulWidget {
  final ReminderContent? initial;
  final ValueChanged<ReminderContent> onChanged;

  const ReminderEditor({super.key, this.initial, required this.onChanged});

  @override
  State<ReminderEditor> createState() => _ReminderEditorState();
}

class _ReminderEditorState extends State<ReminderEditor> {
  late final TextEditingController _dateCtrl;
  late final TextEditingController _endDateCtrl;
  late final TextEditingController _tagCtrl;
  late final TextEditingController _notesCtrl;
  late final TextEditingController _intervalCtrl;
  late final TextEditingController _notifyBeforeCtrl;

  late String _recurrence;
  late String _notifyUnit;
  late bool _notifyEnabled;

  @override
  void initState() {
    super.initState();
    final init = widget.initial;
    _dateCtrl = TextEditingController(text: init?.date ?? '');
    _endDateCtrl = TextEditingController(text: init?.endDate ?? '');
    _tagCtrl = TextEditingController(text: init?.tag ?? '');
    _notesCtrl = TextEditingController(text: init?.notes ?? '');
    _intervalCtrl =
        TextEditingController(text: (init?.recurrenceInterval ?? 1).toString());
    _notifyBeforeCtrl =
        TextEditingController(text: (init?.notifyBefore ?? 30).toString());
    _recurrence = init?.recurrence ?? 'NONE';
    _notifyUnit = init?.notifyUnit ?? 'MINUTES';
    _notifyEnabled = init?.notifyEnabled ?? false;

    for (final ctrl in [
      _dateCtrl,
      _endDateCtrl,
      _tagCtrl,
      _notesCtrl,
      _intervalCtrl,
      _notifyBeforeCtrl,
    ]) {
      ctrl.addListener(_notify);
    }
    WidgetsBinding.instance.addPostFrameCallback((_) => _notify());
  }

  void _notify() {
    widget.onChanged(ReminderContent(
      date: _dateCtrl.text,
      endDate: _endDateCtrl.text.isEmpty ? null : _endDateCtrl.text,
      tag: _tagCtrl.text,
      recurrence: _recurrence,
      recurrenceInterval: int.tryParse(_intervalCtrl.text) ?? 1,
      notes: _notesCtrl.text,
      notifyEnabled: _notifyEnabled,
      notifyBefore: int.tryParse(_notifyBeforeCtrl.text) ?? 30,
      notifyUnit: _notifyUnit,
    ));
  }

  @override
  void dispose() {
    for (final ctrl in [
      _dateCtrl,
      _endDateCtrl,
      _tagCtrl,
      _notesCtrl,
      _intervalCtrl,
      _notifyBeforeCtrl,
    ]) {
      ctrl.removeListener(_notify);
      ctrl.dispose();
    }
    super.dispose();
  }

  Future<void> _pickDateTime(TextEditingController ctrl) async {
    final now = DateTime.now();
    final date = await showDatePicker(
      context: context,
      initialDate: now,
      firstDate: DateTime(2000),
      lastDate: DateTime(2100),
    );
    if (date == null || !mounted) return;
    final time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.now(),
    );
    final dt = time == null
        ? date
        : DateTime(date.year, date.month, date.day, time.hour, time.minute);
    ctrl.text = dt.toIso8601String();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TextField(
          controller: _dateCtrl,
          decoration: InputDecoration(
            labelText: 'Date & Time',
            prefixIcon: const Icon(Icons.calendar_today),
            suffixIcon: IconButton(
              icon: const Icon(Icons.date_range),
              onPressed: () => _pickDateTime(_dateCtrl),
            ),
          ),
          readOnly: true,
          onTap: () => _pickDateTime(_dateCtrl),
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _endDateCtrl,
          decoration: InputDecoration(
            labelText: 'End Date (optional)',
            prefixIcon: const Icon(Icons.event_outlined),
            suffixIcon: IconButton(
              icon: const Icon(Icons.date_range),
              onPressed: () => _pickDateTime(_endDateCtrl),
            ),
          ),
          readOnly: true,
          onTap: () => _pickDateTime(_endDateCtrl),
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _tagCtrl,
          decoration: const InputDecoration(
            labelText: 'Tag / Category',
            prefixIcon: Icon(Icons.label_outline),
          ),
        ),
        const SizedBox(height: 14),
        DropdownButtonFormField<String>(
          value: _recurrence,
          decoration: const InputDecoration(
            labelText: 'Recurrence',
            prefixIcon: Icon(Icons.repeat),
          ),
          items: _recurrenceOptions
              .map((r) => DropdownMenuItem(value: r, child: Text(r)))
              .toList(),
          onChanged: (v) {
            if (v != null) setState(() => _recurrence = v);
            _notify();
          },
        ),
        if (_recurrence != 'NONE') ...[
          const SizedBox(height: 14),
          TextField(
            controller: _intervalCtrl,
            decoration: const InputDecoration(
              labelText: 'Repeat every N',
              prefixIcon: Icon(Icons.numbers),
            ),
            keyboardType: TextInputType.number,
          ),
        ],
        const SizedBox(height: 14),
        SwitchListTile(
          title: const Text('Enable Notification'),
          value: _notifyEnabled,
          onChanged: (v) {
            setState(() => _notifyEnabled = v);
            _notify();
          },
          contentPadding: EdgeInsets.zero,
        ),
        if (_notifyEnabled) ...[
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                flex: 2,
                child: TextField(
                  controller: _notifyBeforeCtrl,
                  decoration: const InputDecoration(labelText: 'Notify Before'),
                  keyboardType: TextInputType.number,
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                flex: 3,
                child: DropdownButtonFormField<String>(
                  value: _notifyUnit,
                  decoration: const InputDecoration(labelText: 'Unit'),
                  items: _notifyUnitOptions
                      .map((u) => DropdownMenuItem(value: u, child: Text(u)))
                      .toList(),
                  onChanged: (v) {
                    if (v != null) setState(() => _notifyUnit = v);
                    _notify();
                  },
                ),
              ),
            ],
          ),
        ],
        const SizedBox(height: 14),
        TextField(
          controller: _notesCtrl,
          maxLines: 4,
          decoration: const InputDecoration(
            labelText: 'Notes',
            alignLabelWithHint: true,
          ),
          textCapitalization: TextCapitalization.sentences,
        ),
      ],
    );
  }
}

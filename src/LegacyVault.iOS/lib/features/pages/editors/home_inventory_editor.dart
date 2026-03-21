import 'package:flutter/material.dart';

import '../../../core/models/page_content.dart';

class HomeInventoryEditor extends StatefulWidget {
  final HomeInventoryContent? initial;
  final ValueChanged<HomeInventoryContent> onChanged;

  const HomeInventoryEditor({super.key, this.initial, required this.onChanged});

  @override
  State<HomeInventoryEditor> createState() => _HomeInventoryEditorState();
}

class _HomeInventoryEditorState extends State<HomeInventoryEditor> {
  late final TextEditingController _itemNameCtrl;
  late final TextEditingController _descriptionCtrl;
  late final TextEditingController _locationCtrl;
  late final TextEditingController _valueCtrl;
  late final TextEditingController _purchaseDateCtrl;
  late final TextEditingController _serialNumberCtrl;
  late final TextEditingController _warrantyExpiryCtrl;

  @override
  void initState() {
    super.initState();
    final init = widget.initial;
    _itemNameCtrl = TextEditingController(text: init?.itemName ?? '');
    _descriptionCtrl = TextEditingController(text: init?.description ?? '');
    _locationCtrl = TextEditingController(text: init?.location ?? '');
    _valueCtrl = TextEditingController(
        text: init?.value != null && init!.value > 0
            ? init.value.toStringAsFixed(2)
            : '');
    _purchaseDateCtrl = TextEditingController(text: init?.purchaseDate ?? '');
    _serialNumberCtrl = TextEditingController(text: init?.serialNumber ?? '');
    _warrantyExpiryCtrl = TextEditingController(text: init?.warrantyExpiry ?? '');

    for (final ctrl in [
      _itemNameCtrl,
      _descriptionCtrl,
      _locationCtrl,
      _valueCtrl,
      _purchaseDateCtrl,
      _serialNumberCtrl,
      _warrantyExpiryCtrl,
    ]) {
      ctrl.addListener(_notify);
    }
    WidgetsBinding.instance.addPostFrameCallback((_) => _notify());
  }

  void _notify() {
    widget.onChanged(HomeInventoryContent(
      itemName: _itemNameCtrl.text,
      description: _descriptionCtrl.text,
      location: _locationCtrl.text,
      value: double.tryParse(_valueCtrl.text) ?? 0.0,
      purchaseDate: _purchaseDateCtrl.text,
      serialNumber: _serialNumberCtrl.text,
      warrantyExpiry: _warrantyExpiryCtrl.text,
    ));
  }

  @override
  void dispose() {
    for (final ctrl in [
      _itemNameCtrl,
      _descriptionCtrl,
      _locationCtrl,
      _valueCtrl,
      _purchaseDateCtrl,
      _serialNumberCtrl,
      _warrantyExpiryCtrl,
    ]) {
      ctrl.removeListener(_notify);
      ctrl.dispose();
    }
    super.dispose();
  }

  Future<void> _pickDate(TextEditingController ctrl) async {
    final now = DateTime.now();
    final picked = await showDatePicker(
      context: context,
      initialDate: now,
      firstDate: DateTime(1900),
      lastDate: DateTime(2100),
    );
    if (picked != null) {
      ctrl.text =
          '${picked.year}-${picked.month.toString().padLeft(2, '0')}-${picked.day.toString().padLeft(2, '0')}';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TextField(
          controller: _itemNameCtrl,
          decoration: const InputDecoration(
            labelText: 'Item Name',
            prefixIcon: Icon(Icons.inventory_2_outlined),
          ),
          textCapitalization: TextCapitalization.words,
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _descriptionCtrl,
          maxLines: 3,
          decoration: const InputDecoration(
            labelText: 'Description',
            alignLabelWithHint: true,
          ),
          textCapitalization: TextCapitalization.sentences,
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _locationCtrl,
          decoration: const InputDecoration(
            labelText: 'Location (room, storage, etc.)',
            prefixIcon: Icon(Icons.place_outlined),
          ),
          textCapitalization: TextCapitalization.words,
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _valueCtrl,
          decoration: const InputDecoration(
            labelText: 'Value (\$)',
            prefixIcon: Icon(Icons.attach_money),
          ),
          keyboardType: const TextInputType.numberWithOptions(decimal: true),
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _purchaseDateCtrl,
          decoration: InputDecoration(
            labelText: 'Purchase Date',
            prefixIcon: const Icon(Icons.calendar_today_outlined),
            suffixIcon: IconButton(
              icon: const Icon(Icons.date_range),
              onPressed: () => _pickDate(_purchaseDateCtrl),
            ),
          ),
          readOnly: true,
          onTap: () => _pickDate(_purchaseDateCtrl),
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _serialNumberCtrl,
          decoration: const InputDecoration(
            labelText: 'Serial Number',
            prefixIcon: Icon(Icons.numbers),
          ),
          autocorrect: false,
        ),
        const SizedBox(height: 14),
        TextField(
          controller: _warrantyExpiryCtrl,
          decoration: InputDecoration(
            labelText: 'Warranty Expiry',
            prefixIcon: const Icon(Icons.shield_outlined),
            suffixIcon: IconButton(
              icon: const Icon(Icons.date_range),
              onPressed: () => _pickDate(_warrantyExpiryCtrl),
            ),
          ),
          readOnly: true,
          onTap: () => _pickDate(_warrantyExpiryCtrl),
        ),
      ],
    );
  }
}

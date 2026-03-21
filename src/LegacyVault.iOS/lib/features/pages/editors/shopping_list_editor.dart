import 'package:flutter/material.dart';

import '../../../core/models/page_content.dart';
import '../../../core/models/shopping_list_item.dart';
import '../../../theme/app_colors.dart';

class ShoppingListEditor extends StatefulWidget {
  final ShoppingListContent? initial;
  final ValueChanged<ShoppingListContent> onChanged;

  const ShoppingListEditor({super.key, this.initial, required this.onChanged});

  @override
  State<ShoppingListEditor> createState() => _ShoppingListEditorState();
}

class _ShoppingListEditorState extends State<ShoppingListEditor> {
  late List<ShoppingListItem> _items;
  late final TextEditingController _notesCtrl;
  final _addItemCtrl = TextEditingController();
  final _addQtyCtrl = TextEditingController();
  int _idCounter = 0;

  @override
  void initState() {
    super.initState();
    _items = List<ShoppingListItem>.from(widget.initial?.items ?? []);
    _notesCtrl = TextEditingController(text: widget.initial?.notes ?? '');
    _notesCtrl.addListener(_notify);
    _idCounter = _items.length;
    WidgetsBinding.instance.addPostFrameCallback((_) => _notify());
  }

  void _notify() {
    widget.onChanged(ShoppingListContent(items: List.from(_items), notes: _notesCtrl.text));
  }

  @override
  void dispose() {
    _notesCtrl.removeListener(_notify);
    _notesCtrl.dispose();
    _addItemCtrl.dispose();
    _addQtyCtrl.dispose();
    super.dispose();
  }

  String _newId() {
    _idCounter++;
    return '${DateTime.now().millisecondsSinceEpoch}_$_idCounter';
  }

  void _addItem() {
    final name = _addItemCtrl.text.trim();
    if (name.isEmpty) return;
    setState(() {
      _items.add(ShoppingListItem(
        id: _newId(),
        name: name,
        quantity: _addQtyCtrl.text.trim(),
        checked: false,
      ));
      _addItemCtrl.clear();
      _addQtyCtrl.clear();
    });
    _notify();
  }

  void _toggleItem(int index) {
    setState(() {
      _items[index] = _items[index].copyWith(checked: !_items[index].checked);
    });
    _notify();
  }

  void _removeItem(int index) {
    setState(() => _items.removeAt(index));
    _notify();
  }

  void _clearChecked() {
    setState(() => _items.removeWhere((item) => item.checked));
    _notify();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final checkedCount = _items.where((i) => i.checked).length;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        // Add item row
        Row(
          children: [
            Expanded(
              flex: 3,
              child: TextField(
                controller: _addItemCtrl,
                decoration: const InputDecoration(
                  hintText: 'Item name',
                  isDense: true,
                  contentPadding:
                      EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                ),
                onSubmitted: (_) => _addItem(),
                textCapitalization: TextCapitalization.sentences,
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              flex: 1,
              child: TextField(
                controller: _addQtyCtrl,
                decoration: const InputDecoration(
                  hintText: 'Qty',
                  isDense: true,
                  contentPadding:
                      EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                ),
                onSubmitted: (_) => _addItem(),
              ),
            ),
            const SizedBox(width: 8),
            ElevatedButton(
              onPressed: _addItem,
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                minimumSize: Size.zero,
              ),
              child: const Icon(Icons.add, size: 20),
            ),
          ],
        ),
        const SizedBox(height: 16),
        // Item count & clear
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              '${_items.length} item(s)',
              style: theme.textTheme.labelLarge?.copyWith(
                color: AppColors.darkSubtext,
              ),
            ),
            if (checkedCount > 0)
              TextButton.icon(
                onPressed: _clearChecked,
                icon: const Icon(Icons.delete_sweep, size: 16),
                label: Text('Clear checked ($checkedCount)'),
                style: TextButton.styleFrom(
                  foregroundColor: AppColors.error,
                  padding: EdgeInsets.zero,
                ),
              ),
          ],
        ),
        const SizedBox(height: 8),
        // Items list
        if (_items.isEmpty)
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.darkCard,
              borderRadius: BorderRadius.circular(10),
            ),
            child: const Center(
              child: Text(
                'No items yet. Add some above.',
                style: TextStyle(color: AppColors.darkSubtext),
              ),
            ),
          )
        else
          Container(
            decoration: BoxDecoration(
              color: AppColors.darkCard,
              borderRadius: BorderRadius.circular(10),
            ),
            child: Column(
              children: _items.asMap().entries.map((e) {
                final index = e.key;
                final item = e.value;
                return Column(
                  children: [
                    ListTile(
                      dense: true,
                      leading: Checkbox(
                        value: item.checked,
                        onChanged: (_) => _toggleItem(index),
                        activeColor: AppColors.accent,
                      ),
                      title: Text(
                        item.name,
                        style: TextStyle(
                          decoration: item.checked
                              ? TextDecoration.lineThrough
                              : TextDecoration.none,
                          color: item.checked
                              ? AppColors.darkSubtext
                              : null,
                        ),
                      ),
                      subtitle: item.quantity.isNotEmpty
                          ? Text(
                              'Qty: ${item.quantity}',
                              style: const TextStyle(
                                  fontSize: 12, color: AppColors.darkSubtext),
                            )
                          : null,
                      trailing: IconButton(
                        icon: const Icon(Icons.close, size: 18),
                        onPressed: () => _removeItem(index),
                        color: AppColors.error,
                      ),
                    ),
                    if (index < _items.length - 1)
                      const Divider(height: 1, indent: 16, endIndent: 16),
                  ],
                );
              }).toList(),
            ),
          ),
        const SizedBox(height: 16),
        TextField(
          controller: _notesCtrl,
          maxLines: 3,
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

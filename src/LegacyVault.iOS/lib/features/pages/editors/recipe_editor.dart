import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/models/page_content.dart';
import '../../../core/models/page_type.dart';
import '../../../core/repositories/page_repository.dart';
import '../../../theme/app_colors.dart';
import '../../pages/page_list_notifier.dart';

class RecipeEditor extends ConsumerStatefulWidget {
  final String categoryId;
  final RecipeContent? initial;
  final ValueChanged<RecipeContent> onChanged;

  const RecipeEditor({
    super.key,
    required this.categoryId,
    this.initial,
    required this.onChanged,
  });

  @override
  ConsumerState<RecipeEditor> createState() => _RecipeEditorState();
}

class _RecipeEditorState extends ConsumerState<RecipeEditor> {
  late List<TextEditingController> _ingredientCtrls;
  late List<TextEditingController> _instructionCtrls;
  late final TextEditingController _servingsCtrl;
  late final TextEditingController _prepCtrl;
  late final TextEditingController _cookCtrl;
  late final TextEditingController _notesCtrl;

  @override
  void initState() {
    super.initState();
    final init = widget.initial;
    final ingredients = init?.ingredients.isNotEmpty == true
        ? init!.ingredients
        : [''];
    final instructions = init?.instructions.isNotEmpty == true
        ? init!.instructions
        : [''];
    _ingredientCtrls =
        ingredients.map((e) => TextEditingController(text: e)).toList();
    _instructionCtrls =
        instructions.map((e) => TextEditingController(text: e)).toList();
    _servingsCtrl =
        TextEditingController(text: (init?.servings ?? 1).toString());
    _prepCtrl = TextEditingController(text: init?.prepTime ?? '');
    _cookCtrl = TextEditingController(text: init?.cookTime ?? '');
    _notesCtrl = TextEditingController(text: init?.notes ?? '');

    for (final ctrl in [
      ..._ingredientCtrls,
      ..._instructionCtrls,
      _servingsCtrl,
      _prepCtrl,
      _cookCtrl,
      _notesCtrl
    ]) {
      ctrl.addListener(_notify);
    }
    WidgetsBinding.instance.addPostFrameCallback((_) => _notify());
  }

  void _notify() {
    widget.onChanged(RecipeContent(
      ingredients:
          _ingredientCtrls.map((c) => c.text).where((s) => s.isNotEmpty).toList(),
      instructions:
          _instructionCtrls.map((c) => c.text).where((s) => s.isNotEmpty).toList(),
      servings: int.tryParse(_servingsCtrl.text) ?? 1,
      prepTime: _prepCtrl.text,
      cookTime: _cookCtrl.text,
      notes: _notesCtrl.text,
    ));
  }

  @override
  void dispose() {
    for (final ctrl in [
      ..._ingredientCtrls,
      ..._instructionCtrls,
      _servingsCtrl,
      _prepCtrl,
      _cookCtrl,
      _notesCtrl
    ]) {
      ctrl.removeListener(_notify);
      ctrl.dispose();
    }
    super.dispose();
  }

  void _addIngredient() {
    final ctrl = TextEditingController();
    ctrl.addListener(_notify);
    setState(() => _ingredientCtrls.add(ctrl));
  }

  void _removeIngredient(int index) {
    _ingredientCtrls[index].removeListener(_notify);
    _ingredientCtrls[index].dispose();
    setState(() => _ingredientCtrls.removeAt(index));
    _notify();
  }

  void _addInstruction() {
    final ctrl = TextEditingController();
    ctrl.addListener(_notify);
    setState(() => _instructionCtrls.add(ctrl));
  }

  void _removeInstruction(int index) {
    _instructionCtrls[index].removeListener(_notify);
    _instructionCtrls[index].dispose();
    setState(() => _instructionCtrls.removeAt(index));
    _notify();
  }

  Future<void> _addIngredientsToShoppingList() async {
    final repo = ref.read(pageRepositoryProvider);
    final shoppingPages = await repo.getPagesByType(PageType.shoppingList);

    if (!mounted) return;

    if (shoppingPages.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
            content: Text('No shopping lists found. Create one first.')),
      );
      return;
    }

    final selectedPage = shoppingPages.length == 1
        ? shoppingPages.first
        : await showDialog(
            context: context,
            builder: (ctx) => SimpleDialog(
              title: const Text('Add to which shopping list?'),
              children: shoppingPages
                  .map((p) => SimpleDialogOption(
                        onPressed: () => Navigator.of(ctx).pop(p),
                        child: Text(p.title),
                      ))
                  .toList(),
            ),
          );

    if (selectedPage == null || !mounted) return;

    final ingredients = _ingredientCtrls
        .map((c) => c.text.trim())
        .where((s) => s.isNotEmpty)
        .toList();
    if (ingredients.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('No ingredients to add')),
      );
      return;
    }

    try {
      final parsed = repo.decryptAndParseContent(selectedPage);
      final existingContent =
          parsed is ShoppingListContent ? parsed : ShoppingListContent(items: [], notes: '');

      int itemCounter = 0;
      String genId() {
        itemCounter++;
        return '${DateTime.now().microsecondsSinceEpoch}_$itemCounter';
      }

      final newItems = ingredients
          .map((ing) => ShoppingListItem(id: genId(), name: ing))
          .toList();

      final updatedContent = existingContent.copyWith(
        items: [...existingContent.items, ...newItems],
      );

      await repo.updatePage(
        page: selectedPage,
        title: selectedPage.title,
        content: updatedContent,
      );

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
                '${ingredients.length} ingredient(s) added to "${selectedPage.title}"'),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to add ingredients: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        // Servings / Times row
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: _servingsCtrl,
                decoration:
                    const InputDecoration(labelText: 'Servings'),
                keyboardType: TextInputType.number,
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: TextField(
                controller: _prepCtrl,
                decoration:
                    const InputDecoration(labelText: 'Prep Time'),
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: TextField(
                controller: _cookCtrl,
                decoration:
                    const InputDecoration(labelText: 'Cook Time'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 20),
        // Ingredients
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text('Ingredients',
                style: theme.textTheme.titleSmall
                    ?.copyWith(fontWeight: FontWeight.bold)),
            TextButton.icon(
              onPressed: _addIngredient,
              icon: const Icon(Icons.add, size: 16),
              label: const Text('Add'),
              style: TextButton.styleFrom(
                  foregroundColor: AppColors.accentLight,
                  padding: EdgeInsets.zero),
            ),
          ],
        ),
        const SizedBox(height: 8),
        ..._ingredientCtrls.asMap().entries.map((e) => Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: Row(
                children: [
                  const Text('•  ', style: TextStyle(fontSize: 18)),
                  Expanded(
                    child: TextField(
                      controller: e.value,
                      decoration: InputDecoration(
                        hintText: 'Ingredient ${e.key + 1}',
                        isDense: true,
                        contentPadding: const EdgeInsets.symmetric(
                            horizontal: 12, vertical: 10),
                      ),
                    ),
                  ),
                  if (_ingredientCtrls.length > 1)
                    IconButton(
                      icon: const Icon(Icons.remove_circle_outline,
                          color: AppColors.error, size: 20),
                      onPressed: () => _removeIngredient(e.key),
                      padding: EdgeInsets.zero,
                      constraints:
                          const BoxConstraints(minWidth: 32, minHeight: 32),
                    ),
                ],
              ),
            )),
        const SizedBox(height: 4),
        OutlinedButton.icon(
          onPressed: _addIngredientsToShoppingList,
          icon: const Icon(Icons.shopping_cart_outlined, size: 18),
          label: const Text('Add Ingredients to Shopping List'),
          style: OutlinedButton.styleFrom(
            foregroundColor: AppColors.accentLight,
            side: const BorderSide(color: AppColors.accentLight),
          ),
        ),
        const SizedBox(height: 20),
        // Instructions
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text('Instructions',
                style: theme.textTheme.titleSmall
                    ?.copyWith(fontWeight: FontWeight.bold)),
            TextButton.icon(
              onPressed: _addInstruction,
              icon: const Icon(Icons.add, size: 16),
              label: const Text('Add'),
              style: TextButton.styleFrom(
                  foregroundColor: AppColors.accentLight,
                  padding: EdgeInsets.zero),
            ),
          ],
        ),
        const SizedBox(height: 8),
        ..._instructionCtrls.asMap().entries.map((e) => Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Padding(
                    padding: const EdgeInsets.only(top: 12, right: 6),
                    child: Text(
                      '${e.key + 1}.',
                      style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          color: AppColors.accentLight),
                    ),
                  ),
                  Expanded(
                    child: TextField(
                      controller: e.value,
                      maxLines: 3,
                      minLines: 1,
                      decoration: InputDecoration(
                        hintText: 'Step ${e.key + 1}',
                        isDense: true,
                        contentPadding: const EdgeInsets.symmetric(
                            horizontal: 12, vertical: 10),
                      ),
                    ),
                  ),
                  if (_instructionCtrls.length > 1)
                    IconButton(
                      icon: const Icon(Icons.remove_circle_outline,
                          color: AppColors.error, size: 20),
                      onPressed: () => _removeInstruction(e.key),
                      padding: EdgeInsets.zero,
                      constraints:
                          const BoxConstraints(minWidth: 32, minHeight: 32),
                    ),
                ],
              ),
            )),
        const SizedBox(height: 20),
        TextField(
          controller: _notesCtrl,
          maxLines: 4,
          decoration: const InputDecoration(
            labelText: 'Notes',
            alignLabelWithHint: true,
          ),
        ),
      ],
    );
  }
}

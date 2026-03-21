import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/models/page_model.dart';
import '../../shared/widgets/page_card.dart';
import '../../theme/app_colors.dart';
import 'search_notifier.dart';

class SearchScreen extends ConsumerStatefulWidget {
  const SearchScreen({super.key});

  @override
  ConsumerState<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends ConsumerState<SearchScreen> {
  final _searchCtrl = TextEditingController();

  @override
  void dispose() {
    _searchCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(searchNotifierProvider);
    final notifier = ref.read(searchNotifierProvider.notifier);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Search', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
            child: TextField(
              controller: _searchCtrl,
              autofocus: true,
              decoration: InputDecoration(
                hintText: 'Search page titles...',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _searchCtrl.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () {
                          _searchCtrl.clear();
                          notifier.clear();
                        },
                      )
                    : null,
              ),
              onChanged: (q) => notifier.search(q),
            ),
          ),
          const SizedBox(height: 8),
          Expanded(
            child: state.query.isEmpty
                ? _HintState()
                : state.isLoading
                    ? const Center(child: CircularProgressIndicator())
                    : state.results.isEmpty
                        ? _NoResults(query: state.query)
                        : _ResultsList(results: state.results),
          ),
        ],
      ),
    );
  }
}

class _ResultsList extends StatelessWidget {
  final List<PageModel> results;

  const _ResultsList({required this.results});

  @override
  Widget build(BuildContext context) {
    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: results.length,
      separatorBuilder: (_, __) => const SizedBox(height: 10),
      itemBuilder: (context, index) {
        final page = results[index];
        final summary = PageSummary.fromPageModel(page);
        return PageCard(
          page: summary,
          onTap: () => context
              .push('/vaults/${page.categoryId}/pages/${page.id}'),
        );
      },
    );
  }
}

class _HintState extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.search, size: 72, color: AppColors.darkSubtext),
          const SizedBox(height: 16),
          Text(
            'Search your vault pages',
            style: Theme.of(context)
                .textTheme
                .titleMedium
                ?.copyWith(color: AppColors.darkSubtext),
          ),
          const SizedBox(height: 8),
          Text(
            'Results are matched on page titles',
            style: Theme.of(context)
                .textTheme
                .bodySmall
                ?.copyWith(color: AppColors.darkSubtext),
          ),
        ],
      ),
    );
  }
}

class _NoResults extends StatelessWidget {
  final String query;

  const _NoResults({required this.query});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.search_off, size: 64, color: AppColors.darkSubtext),
          const SizedBox(height: 16),
          Text(
            'No results for "$query"',
            style: Theme.of(context)
                .textTheme
                .titleMedium
                ?.copyWith(color: AppColors.darkSubtext),
          ),
        ],
      ),
    );
  }
}

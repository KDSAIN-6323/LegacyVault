import type { Page } from '../types';

function stripHtml(html: string): string {
  return html.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim();
}

function extractTokens(page: Page): { tokens: string[]; tags: string[] } {
  const tokens: string[] = [page.title.toLowerCase()];
  const tags: string[] = [];

  if (page.isEncrypted || !page.content) return { tokens, tags };

  try {
    const c = JSON.parse(page.content);

    // Tags (Quote)
    if (Array.isArray(c.tags)) {
      const normalised = (c.tags as string[]).map((t) => t.toLowerCase());
      tags.push(...normalised);
      tokens.push(...normalised);
    }

    // Plaintext fields
    const strings: (string | undefined)[] = [
      c.text,        // Quote text
      c.author,      // Quote author
      c.source,      // Quote source
      c.notes,       // Reminder / Recipe notes
      c.itemName,    // HomeInventory
      c.description, // HomeInventory
      c.location,    // HomeInventory
      c.date,        // Reminder date string
      c.tag,         // Reminder tag enum value
      c.url,         // Password URL
      c.username,    // Password username (not the password itself)
    ];
    for (const s of strings) {
      if (typeof s === 'string' && s) tokens.push(s.toLowerCase());
    }

    // Note body (TipTap HTML)
    if (typeof c.body === 'string') tokens.push(stripHtml(c.body).toLowerCase());

    // Recipe arrays
    if (Array.isArray(c.ingredients)) {
      tokens.push(...(c.ingredients as string[]).map((s) => String(s).toLowerCase()));
    }
    if (Array.isArray(c.instructions)) {
      tokens.push(...(c.instructions as string[]).map((s) => String(s).toLowerCase()));
    }

    // Shopping list item names
    if (Array.isArray(c.items)) {
      tokens.push(...(c.items as Array<{ name?: string }>)
        .map((item) => String(item.name ?? '').toLowerCase())
        .filter(Boolean));
    }
  } catch {
    // content is not valid JSON (encrypted or malformed) — title-only search
  }

  return { tokens, tags };
}

export function filterPages(pages: Page[], query: string): Page[] {
  if (!query.trim()) return pages;
  const q = query.trim().toLowerCase();
  return pages.filter((page) => extractTokens(page).tokens.some((t) => t.includes(q)));
}

/** Returns tags on the page that match the query — for display in the result row. */
export function matchingTags(page: Page, query: string): string[] {
  if (!query.trim()) return [];
  const q = query.trim().toLowerCase();
  return extractTokens(page).tags.filter((t) => t.includes(q));
}

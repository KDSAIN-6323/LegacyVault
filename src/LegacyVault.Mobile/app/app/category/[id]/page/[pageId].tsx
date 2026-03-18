import React, { useEffect, useState } from 'react';
import {
  View, Text, ScrollView, StyleSheet, ActivityIndicator,
  TouchableOpacity, Alert, Clipboard,
} from 'react-native';
import { useLocalSearchParams, useNavigation } from 'expo-router';
import { pagesApi } from '../../../../../src/api/pagesApi';
import { cryptoService } from '../../../../../src/crypto/cryptoService.native';
import { keyCache } from '../../../../../src/crypto/keyCache';
import type {
  Page, RecipeContent, QuoteContent, NoteContent,
  HomeInventoryContent, PasswordContent, ReminderContent,
  ShoppingListContent,
} from '../../../../../src/types';

// ── helpers ──────────────────────────────────────────────────────

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>{title}</Text>
      {children}
    </View>
  );
}

function Field({ label, value }: { label: string; value?: string | number | null }) {
  if (value === undefined || value === null || value === '') return null;
  return (
    <View style={styles.field}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <Text style={styles.fieldValue}>{String(value)}</Text>
    </View>
  );
}

function CopyField({ label, value, secret }: { label: string; value?: string; secret?: boolean }) {
  const [revealed, setRevealed] = useState(false);
  if (!value) return null;
  return (
    <View style={styles.field}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <View style={styles.copyRow}>
        <Text style={[styles.fieldValue, styles.copyValue]}>
          {secret && !revealed ? '••••••••' : value}
        </Text>
        <View style={styles.copyActions}>
          {secret && (
            <TouchableOpacity onPress={() => setRevealed((r) => !r)} style={styles.iconBtn}>
              <Text style={styles.iconBtnText}>{revealed ? '🙈' : '👁️'}</Text>
            </TouchableOpacity>
          )}
          <TouchableOpacity onPress={() => { Clipboard.setString(value); Alert.alert('Copied!'); }} style={styles.iconBtn}>
            <Text style={styles.iconBtnText}>📋</Text>
          </TouchableOpacity>
        </View>
      </View>
    </View>
  );
}

// ── Content renderers ─────────────────────────────────────────────

function RecipeView({ c }: { c: RecipeContent }) {
  return (
    <>
      <View style={styles.recipeMeta}>
        {c.servings != null && <Text style={styles.chip}>🍽 {c.servings} servings</Text>}
        {c.prepTime && <Text style={styles.chip}>⏱ Prep {c.prepTime}</Text>}
        {c.cookTime && <Text style={styles.chip}>🔥 Cook {c.cookTime}</Text>}
      </View>
      {c.ingredients?.length > 0 && (
        <Section title="Ingredients">
          {c.ingredients.map((ing, i) => (
            <Text key={i} style={styles.listItem}>• {ing}</Text>
          ))}
        </Section>
      )}
      {c.instructions?.length > 0 && (
        <Section title="Instructions">
          {c.instructions.map((step, i) => (
            <View key={i} style={styles.stepRow}>
              <Text style={styles.stepNum}>{i + 1}</Text>
              <Text style={styles.stepText}>{step}</Text>
            </View>
          ))}
        </Section>
      )}
      {c.notes && <Section title="Notes"><Text style={styles.body}>{c.notes}</Text></Section>}
    </>
  );
}

function QuoteView({ c }: { c: QuoteContent }) {
  return (
    <>
      <View style={styles.quoteBlock}>
        <Text style={styles.quoteText}>"{c.text}"</Text>
        {c.author && <Text style={styles.quoteAuthor}>— {c.author}</Text>}
        {c.source && <Text style={styles.quoteSource}>{c.source}</Text>}
      </View>
      {c.tags && c.tags.length > 0 && (
        <View style={styles.tagRow}>
          {c.tags.map((t) => (
            <Text key={t} style={styles.tag}>{t}</Text>
          ))}
        </View>
      )}
    </>
  );
}

function NoteView({ c }: { c: NoteContent }) {
  return <Text style={styles.body}>{c.body}</Text>;
}

function HomeInventoryView({ c }: { c: HomeInventoryContent }) {
  return (
    <>
      <Field label="Item" value={c.itemName} />
      <Field label="Description" value={c.description} />
      <Field label="Location" value={c.location} />
      {c.value != null && <Field label="Value" value={`$${c.value.toFixed(2)}`} />}
      <Field label="Purchase Date" value={c.purchaseDate} />
      <Field label="Serial Number" value={c.serialNumber} />
      <Field label="Warranty Expiry" value={c.warrantyExpiry} />
    </>
  );
}

function PasswordView({ c }: { c: PasswordContent }) {
  return (
    <>
      <Field label="URL" value={c.url} />
      <CopyField label="Username" value={c.username} />
      <CopyField label="Password" value={c.password} secret />
      {c.totp && <CopyField label="TOTP Secret" value={c.totp} secret />}
      {c.notes && <Field label="Notes" value={c.notes} />}
    </>
  );
}

const TAG_LABELS: Record<string, string> = {
  birthday: '🎂 Birthday',
  anniversary: '💍 Anniversary',
  holiday: '🎉 Holiday',
  appointment: '📅 Appointment',
  custom: '🔔 Custom',
};

const RECURRENCE_LABELS: Record<string, string> = {
  once: 'One time',
  weekly: 'Weekly',
  monthly: 'Monthly',
  yearly: 'Yearly',
};

function ReminderView({ c }: { c: ReminderContent }) {
  const fmt = (d: string) => new Date(d + 'T00:00:00').toLocaleDateString('default', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
  });
  const isRange = !!c.endDate && c.endDate > c.date;
  return (
    <>
      {isRange ? (
        <>
          <Field label="Start Date" value={fmt(c.date)} />
          <Field label="End Date" value={fmt(c.endDate!)} />
        </>
      ) : (
        <Field label="Date" value={fmt(c.date)} />
      )}
      <Field label="Type" value={c.tag ? TAG_LABELS[c.tag] : undefined} />
      <Field label="Recurrence" value={c.recurrence ? RECURRENCE_LABELS[c.recurrence] : undefined} />
      {c.notifyEnabled && (
        <Field label="Reminder" value={`${c.notifyBefore} ${c.notifyUnit} before`} />
      )}
      {c.notes && <Field label="Notes" value={c.notes} />}
    </>
  );
}

function ShoppingListView({ c }: { c: ShoppingListContent }) {
  const [checked, setChecked] = useState<Set<number>>(
    () => new Set(c.items.reduce<number[]>((acc, it, i) => (it.checked ? [...acc, i] : acc), []))
  );
  const toggle = (i: number) =>
    setChecked((prev) => {
      const next = new Set(prev);
      next.has(i) ? next.delete(i) : next.add(i);
      return next;
    });
  return (
    <>
      {c.items.map((item, i) => (
        <TouchableOpacity key={i} style={styles.checkItem} onPress={() => toggle(i)}>
          <Text style={styles.checkBox}>{checked.has(i) ? '✅' : '⬜'}</Text>
          <Text style={[styles.checkText, checked.has(i) && styles.checkTextDone]}>
            {item.text}
          </Text>
        </TouchableOpacity>
      ))}
      {c.notes ? <Field label="Notes" value={c.notes} /> : null}
    </>
  );
}

// ── Main screen ───────────────────────────────────────────────────

export default function PageDetailScreen() {
  const { id, pageId } = useLocalSearchParams<{ id: string; pageId: string }>();
  const navigation = useNavigation();
  const [page, setPage] = useState<Page | null>(null);
  const [content, setContent] = useState<Record<string, unknown> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id || !pageId) return;
    (async () => {
      try {
        const { data } = await pagesApi.getOne(id, pageId);
        setPage(data);
        navigation.setOptions({ title: data.title });

        let raw = data.content;
        if (data.isEncrypted && data.encryptionIV) {
          const key = keyCache.get(id);
          if (!key) { setError('Vault is locked. Go back and unlock it.'); setLoading(false); return; }
          raw = await cryptoService.decrypt(data.content, data.encryptionIV, key);
        }
        setContent(JSON.parse(raw));
      } catch (e: unknown) {
        setError(e instanceof Error ? e.message : 'Failed to load page.');
      } finally {
        setLoading(false);
      }
    })();
  }, [id, pageId, navigation]);

  if (loading) {
    return <View style={styles.center}><ActivityIndicator color="#89b4fa" size="large" /></View>;
  }

  if (error || !page || !content) {
    return (
      <View style={styles.center}>
        <Text style={styles.errorText}>{error || 'Page not found.'}</Text>
      </View>
    );
  }

  const renderContent = () => {
    switch (page.type) {
      case 'Recipe':      return <RecipeView c={content as unknown as RecipeContent} />;
      case 'Quote':       return <QuoteView c={content as unknown as QuoteContent} />;
      case 'Note':        return <NoteView c={content as unknown as NoteContent} />;
      case 'HomeInventory': return <HomeInventoryView c={content as unknown as HomeInventoryContent} />;
      case 'Password':    return <PasswordView c={content as unknown as PasswordContent} />;
      case 'Reminder':    return <ReminderView c={content as unknown as ReminderContent} />;
      case 'ShoppingList': return <ShoppingListView c={content as unknown as ShoppingListContent} />;
      default:            return <Text style={styles.body}>{JSON.stringify(content, null, 2)}</Text>;
    }
  };

  const typeIcons: Record<string, string> = {
    Recipe: '🍳', Quote: '💬', Note: '📝', HomeInventory: '🏠',
    Password: '🔑', Reminder: '🔔', ShoppingList: '🛒',
  };

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.scrollContent}>
      <View style={styles.pageHeader}>
        <Text style={styles.pageTypeIcon}>{typeIcons[page.type] ?? '📄'}</Text>
        <View style={styles.pageHeaderText}>
          <Text style={styles.pageTitle}>{page.title}</Text>
          <Text style={styles.pageType}>{page.type}{page.isEncrypted ? ' · 🔒' : ''}</Text>
        </View>
      </View>
      <View style={styles.divider} />
      {renderContent()}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: '#1e1e2e' },
  scrollContent: { padding: 20, paddingBottom: 48 },
  center: { flex: 1, backgroundColor: '#1e1e2e', alignItems: 'center', justifyContent: 'center' },
  errorText: { color: '#f38ba8', fontSize: 15, textAlign: 'center', paddingHorizontal: 32 },

  // Page header
  pageHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 16 },
  pageTypeIcon: { fontSize: 36, marginRight: 14 },
  pageHeaderText: { flex: 1 },
  pageTitle: { fontSize: 22, fontWeight: '700', color: '#cdd6f4', lineHeight: 28 },
  pageType: { fontSize: 13, color: '#6c7086', marginTop: 4 },
  divider: { height: 1, backgroundColor: '#313244', marginBottom: 20 },

  // Section
  section: { marginBottom: 20 },
  sectionTitle: {
    fontSize: 12, fontWeight: '700', color: '#89b4fa',
    textTransform: 'uppercase', letterSpacing: 0.8, marginBottom: 10,
  },

  // Field
  field: { marginBottom: 16 },
  fieldLabel: { fontSize: 12, color: '#6c7086', fontWeight: '600', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 4 },
  fieldValue: { fontSize: 15, color: '#cdd6f4', lineHeight: 22 },

  // Copy field
  copyRow: { flexDirection: 'row', alignItems: 'center' },
  copyValue: { flex: 1 },
  copyActions: { flexDirection: 'row', gap: 8 },
  iconBtn: { padding: 4 },
  iconBtnText: { fontSize: 18 },

  // Body text
  body: { fontSize: 15, color: '#cdd6f4', lineHeight: 24 },

  // Recipe
  recipeMeta: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 20 },
  chip: {
    backgroundColor: '#313244', color: '#cdd6f4', paddingHorizontal: 10,
    paddingVertical: 5, borderRadius: 20, fontSize: 13,
  },
  listItem: { fontSize: 14, color: '#cdd6f4', marginBottom: 6, lineHeight: 20 },
  stepRow: { flexDirection: 'row', marginBottom: 12, gap: 10 },
  stepNum: {
    fontSize: 13, fontWeight: '700', color: '#89b4fa',
    backgroundColor: '#313244', width: 24, height: 24, borderRadius: 12,
    textAlign: 'center', lineHeight: 24,
  },
  stepText: { flex: 1, fontSize: 14, color: '#cdd6f4', lineHeight: 22 },

  // Quote
  quoteBlock: {
    backgroundColor: '#2a2a3e', borderLeftWidth: 4, borderLeftColor: '#cba6f7',
    borderRadius: 6, padding: 16, marginBottom: 16,
  },
  quoteText: { fontSize: 18, color: '#cdd6f4', fontStyle: 'italic', lineHeight: 28, marginBottom: 12 },
  quoteAuthor: { fontSize: 14, color: '#a6e3a1', fontWeight: '600' },
  quoteSource: { fontSize: 13, color: '#6c7086', marginTop: 2 },
  tagRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  tag: {
    backgroundColor: '#313244', color: '#89b4fa', paddingHorizontal: 10,
    paddingVertical: 4, borderRadius: 20, fontSize: 12,
  },

  // Shopping list
  checkItem: { flexDirection: 'row', alignItems: 'center', paddingVertical: 10, gap: 12 },
  checkBox: { fontSize: 20 },
  checkText: { fontSize: 15, color: '#cdd6f4', flex: 1 },
  checkTextDone: { textDecorationLine: 'line-through', color: '#6c7086' },
});

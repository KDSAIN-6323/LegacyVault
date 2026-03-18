import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, Modal, TextInput, Alert, Pressable,
} from 'react-native';
import { router, useLocalSearchParams, useNavigation } from 'expo-router';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../../../src/store';
import { fetchPages } from '../../../src/store/pagesSlice';
import { cryptoService } from '../../../src/crypto/cryptoService.native';
import { keyCache } from '../../../src/crypto/keyCache';
import { PageSummary } from '../../../src/types';

const PAGE_TYPE_ICONS: Record<string, string> = {
  Recipe: '🍳',
  Quote: '💬',
  Note: '📝',
  HomeInventory: '🏠',
  Password: '🔑',
  Reminder: '🔔',
  ShoppingList: '🛒',
};

export default function CategoryScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const dispatch = useDispatch<AppDispatch>();
  const navigation = useNavigation();

  const category = useSelector((s: RootState) =>
    s.categories.items.find((c) => c.id === id)
  );
  const pages = useSelector((s: RootState) => s.pages.byCategory[id ?? ''] ?? []);
  const pagesStatus = useSelector((s: RootState) => s.pages.status);

  const [unlocked, setUnlocked] = useState(() => !category?.isEncrypted || keyCache.has(id ?? ''));
  const [showUnlock, setShowUnlock] = useState(() => !!category?.isEncrypted && !keyCache.has(id ?? ''));
  const [password, setPassword] = useState('');
  const [unlocking, setUnlocking] = useState(false);
  const [unlockError, setUnlockError] = useState('');

  // Update nav title when category loads
  useEffect(() => {
    if (category) {
      navigation.setOptions({ title: `${category.icon} ${category.name}` });
    }
  }, [category, navigation]);

  // Fetch pages once unlocked
  useEffect(() => {
    if (unlocked && id) {
      dispatch(fetchPages(id));
    }
  }, [unlocked, id, dispatch]);

  const handleUnlock = useCallback(async () => {
    if (!password || !category?.encryptionSalt || !id) return;
    setUnlocking(true);
    setUnlockError('');
    try {
      const key = await cryptoService.deriveKey(password, category.encryptionSalt);
      keyCache.set(id, key);
      setUnlocked(true);
      setShowUnlock(false);
      setPassword('');
    } catch {
      setUnlockError('Incorrect password.');
    } finally {
      setUnlocking(false);
    }
  }, [password, category, id]);

  const renderItem = ({ item }: { item: PageSummary }) => (
    <TouchableOpacity
      style={styles.item}
      onPress={() => router.push(`/app/category/${id}/page/${item.id}`)}
    >
      <Text style={styles.typeIcon}>{PAGE_TYPE_ICONS[item.type] ?? '📄'}</Text>
      <View style={styles.info}>
        <Text style={styles.title}>{item.title}</Text>
        <Text style={styles.meta}>
          {item.type}
          {item.isEncrypted ? ' · 🔒' : ''}
        </Text>
      </View>
      <Text style={styles.arrow}>›</Text>
    </TouchableOpacity>
  );

  if (!category) {
    return (
      <View style={styles.center}>
        <ActivityIndicator color="#89b4fa" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {/* Encryption unlock modal */}
      <Modal visible={showUnlock} transparent animationType="fade">
        <Pressable style={styles.overlay} onPress={() => router.back()}>
          <Pressable style={styles.unlockCard} onPress={() => {}}>
            <Text style={styles.lockIcon}>🔒</Text>
            <Text style={styles.unlockTitle}>{category.icon} {category.name}</Text>
            <Text style={styles.unlockSub}>This vault is encrypted. Enter the vault password to unlock.</Text>

            <TextInput
              style={[styles.input, unlockError ? styles.inputError : null]}
              placeholder="Vault password"
              placeholderTextColor="#6c7086"
              value={password}
              onChangeText={(t) => { setPassword(t); setUnlockError(''); }}
              secureTextEntry
              autoFocus
              onSubmitEditing={handleUnlock}
            />
            {unlockError ? <Text style={styles.errorText}>{unlockError}</Text> : null}

            <TouchableOpacity
              style={[styles.unlockBtn, unlocking && styles.btnDisabled]}
              onPress={handleUnlock}
              disabled={unlocking}
            >
              {unlocking
                ? <ActivityIndicator color="#1e1e2e" />
                : <Text style={styles.unlockBtnText}>Unlock</Text>}
            </TouchableOpacity>

            <TouchableOpacity onPress={() => router.back()} style={styles.cancelBtn}>
              <Text style={styles.cancelText}>Cancel</Text>
            </TouchableOpacity>
          </Pressable>
        </Pressable>
      </Modal>

      {/* Pages list */}
      {pagesStatus === 'loading' && pages.length === 0 && (
        <View style={styles.center}>
          <ActivityIndicator color="#89b4fa" />
        </View>
      )}

      {unlocked && pagesStatus !== 'loading' && pages.length === 0 && (
        <View style={styles.emptyState}>
          <Text style={styles.emptyIcon}>📄</Text>
          <Text style={styles.emptyText}>No pages yet.</Text>
          <Text style={styles.emptyHint}>Pages created on the web app will appear here.</Text>
        </View>
      )}

      <FlatList
        data={pages}
        keyExtractor={(p) => p.id}
        renderItem={renderItem}
        contentContainerStyle={styles.list}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#1e1e2e' },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  list: { padding: 12 },
  item: {
    flexDirection: 'row', alignItems: 'center', padding: 16, marginBottom: 8,
    backgroundColor: '#2a2a3e', borderRadius: 10,
  },
  typeIcon: { fontSize: 22, marginRight: 12 },
  info: { flex: 1 },
  title: { fontSize: 15, fontWeight: '600', color: '#cdd6f4' },
  meta: { fontSize: 12, color: '#6c7086', marginTop: 2 },
  arrow: { color: '#6c7086', fontSize: 20 },
  emptyState: { flex: 1, alignItems: 'center', justifyContent: 'center', paddingTop: 80 },
  emptyIcon: { fontSize: 48, marginBottom: 12 },
  emptyText: { fontSize: 18, color: '#cdd6f4', fontWeight: '600' },
  emptyHint: { fontSize: 14, color: '#6c7086', marginTop: 8, textAlign: 'center', paddingHorizontal: 32 },
  // Unlock modal
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.7)', alignItems: 'center', justifyContent: 'center', padding: 24 },
  unlockCard: {
    backgroundColor: '#2a2a3e', borderRadius: 16, padding: 28,
    width: '100%', alignItems: 'center',
  },
  lockIcon: { fontSize: 40, marginBottom: 12 },
  unlockTitle: { fontSize: 20, fontWeight: '700', color: '#cdd6f4', marginBottom: 8 },
  unlockSub: { fontSize: 14, color: '#6c7086', textAlign: 'center', marginBottom: 20, lineHeight: 20 },
  input: {
    backgroundColor: '#313244', color: '#cdd6f4', padding: 14, borderRadius: 8,
    width: '100%', fontSize: 16, marginBottom: 8,
  },
  inputError: { borderWidth: 1, borderColor: '#f38ba8' },
  errorText: { color: '#f38ba8', fontSize: 13, marginBottom: 8, alignSelf: 'flex-start' },
  unlockBtn: {
    backgroundColor: '#89b4fa', padding: 14, borderRadius: 8,
    width: '100%', alignItems: 'center', marginTop: 8,
  },
  btnDisabled: { opacity: 0.6 },
  unlockBtnText: { color: '#1e1e2e', fontWeight: '700', fontSize: 16 },
  cancelBtn: { marginTop: 14 },
  cancelText: { color: '#6c7086', fontSize: 15 },
});

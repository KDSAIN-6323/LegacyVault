import React, { useEffect, useState } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, Modal, TextInput, Switch, Alert, Pressable,
} from 'react-native';
import { router } from 'expo-router';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../../src/store';
import { fetchCategories, selectCategory, createCategory } from '../../src/store/categoriesSlice';
import { cryptoService } from '../../src/crypto/cryptoService.native';
import { Category } from '../../src/types';

export default function CategoriesScreen() {
  const dispatch = useDispatch<AppDispatch>();
  const { items, status } = useSelector((s: RootState) => s.categories);
  const [showNew, setShowNew] = useState(false);
  const [newName, setNewName] = useState('');
  const [newIcon, setNewIcon] = useState('📁');
  const [newEncrypted, setNewEncrypted] = useState(false);
  const [newPassword, setNewPassword] = useState('');
  const [creating, setCreating] = useState(false);

  useEffect(() => { dispatch(fetchCategories()); }, []);

  const handleSelectCategory = (cat: Category) => {
    dispatch(selectCategory(cat.id));
    router.push(`/app/category/${cat.id}`);
  };

  const handleCreate = async () => {
    if (!newName.trim()) return;
    if (newEncrypted && newPassword.length < 8) {
      Alert.alert('Weak Password', 'Password must be at least 8 characters.');
      return;
    }
    setCreating(true);
    try {
      let salt: string | undefined;
      if (newEncrypted) {
        salt = cryptoService.generateSalt();
      }
      await dispatch(createCategory({
        name: newName.trim(),
        icon: newIcon,
        isEncrypted: newEncrypted,
        encryptionSalt: salt,
      })).unwrap();
      setShowNew(false);
      setNewName('');
      setNewIcon('📁');
      setNewEncrypted(false);
      setNewPassword('');
    } catch {
      Alert.alert('Error', 'Could not create vault.');
    } finally {
      setCreating(false);
    }
  };

  const renderItem = ({ item }: { item: Category }) => (
    <TouchableOpacity style={styles.item} onPress={() => handleSelectCategory(item)}>
      <Text style={styles.icon}>{item.icon}</Text>
      <View style={styles.info}>
        <Text style={styles.name}>{item.name}</Text>
        <Text style={styles.meta}>
          {item.pageCount} page{item.pageCount !== 1 ? 's' : ''}
          {item.isEncrypted ? ' · 🔒 Encrypted' : ''}
        </Text>
      </View>
      <Text style={styles.arrow}>›</Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>🗄️ LegacyVault</Text>
        <TouchableOpacity onPress={() => setShowNew(true)}>
          <Text style={styles.addBtn}>+ New</Text>
        </TouchableOpacity>
      </View>

      {status === 'loading' && items.length === 0 && (
        <ActivityIndicator color="#89b4fa" style={{ marginTop: 40 }} />
      )}

      {status === 'failed' && (
        <View style={styles.emptyState}>
          <Text style={styles.emptyIcon}>⚠️</Text>
          <Text style={styles.emptyText}>Could not load vaults.</Text>
          <TouchableOpacity onPress={() => dispatch(fetchCategories())} style={styles.retryBtn}>
            <Text style={styles.retryText}>Retry</Text>
          </TouchableOpacity>
        </View>
      )}

      {status !== 'loading' && status !== 'failed' && items.length === 0 && (
        <View style={styles.emptyState}>
          <Text style={styles.emptyIcon}>📂</Text>
          <Text style={styles.emptyText}>No vaults yet.</Text>
          <Text style={styles.emptyHint}>Tap + New to create your first vault.</Text>
        </View>
      )}

      <FlatList
        data={items}
        keyExtractor={(i) => i.id}
        renderItem={renderItem}
        contentContainerStyle={styles.list}
      />

      {/* New Vault Modal */}
      <Modal visible={showNew} transparent animationType="slide">
        <Pressable style={styles.modalOverlay} onPress={() => setShowNew(false)}>
          <Pressable style={styles.modalSheet} onPress={() => {}}>
            <Text style={styles.modalTitle}>New Vault</Text>

            <Text style={styles.label}>Icon</Text>
            <TextInput
              style={[styles.input, styles.iconInput]}
              value={newIcon}
              onChangeText={setNewIcon}
              maxLength={2}
            />

            <Text style={styles.label}>Name</Text>
            <TextInput
              style={styles.input}
              placeholder="e.g. Family Recipes"
              placeholderTextColor="#6c7086"
              value={newName}
              onChangeText={setNewName}
              autoFocus
            />

            <View style={styles.row}>
              <Text style={styles.label}>Encrypted 🔒</Text>
              <Switch
                value={newEncrypted}
                onValueChange={setNewEncrypted}
                thumbColor={newEncrypted ? '#89b4fa' : '#6c7086'}
                trackColor={{ false: '#313244', true: '#45475a' }}
              />
            </View>

            {newEncrypted && (
              <>
                <Text style={styles.label}>Vault Password</Text>
                <TextInput
                  style={styles.input}
                  placeholder="Min 8 characters"
                  placeholderTextColor="#6c7086"
                  value={newPassword}
                  onChangeText={setNewPassword}
                  secureTextEntry
                />
              </>
            )}

            <TouchableOpacity
              style={[styles.createBtn, creating && styles.createBtnDisabled]}
              onPress={handleCreate}
              disabled={creating}
            >
              {creating
                ? <ActivityIndicator color="#1e1e2e" />
                : <Text style={styles.createBtnText}>Create Vault</Text>}
            </TouchableOpacity>

            <TouchableOpacity onPress={() => setShowNew(false)} style={styles.cancelBtn}>
              <Text style={styles.cancelText}>Cancel</Text>
            </TouchableOpacity>
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#1e1e2e' },
  header: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    padding: 20, paddingTop: 60, borderBottomWidth: 1, borderBottomColor: '#313244',
  },
  title: { fontSize: 22, fontWeight: '700', color: '#cdd6f4' },
  addBtn: { color: '#89b4fa', fontSize: 16, fontWeight: '600' },
  list: { padding: 12 },
  item: {
    flexDirection: 'row', alignItems: 'center', padding: 16, marginBottom: 8,
    backgroundColor: '#2a2a3e', borderRadius: 10,
  },
  icon: { fontSize: 24, marginRight: 12 },
  info: { flex: 1 },
  name: { fontSize: 16, fontWeight: '600', color: '#cdd6f4' },
  meta: { fontSize: 12, color: '#6c7086', marginTop: 2 },
  arrow: { color: '#6c7086', fontSize: 20 },
  emptyState: { flex: 1, alignItems: 'center', justifyContent: 'center', paddingTop: 80 },
  emptyIcon: { fontSize: 48, marginBottom: 12 },
  emptyText: { fontSize: 18, color: '#cdd6f4', fontWeight: '600' },
  emptyHint: { fontSize: 14, color: '#6c7086', marginTop: 8 },
  retryBtn: { marginTop: 16, paddingHorizontal: 24, paddingVertical: 10, backgroundColor: '#313244', borderRadius: 8 },
  retryText: { color: '#89b4fa', fontWeight: '600' },
  // Modal
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.6)', justifyContent: 'flex-end' },
  modalSheet: {
    backgroundColor: '#2a2a3e', borderTopLeftRadius: 20, borderTopRightRadius: 20,
    padding: 24, paddingBottom: 40,
  },
  modalTitle: { fontSize: 20, fontWeight: '700', color: '#cdd6f4', marginBottom: 20 },
  label: { fontSize: 13, color: '#6c7086', marginBottom: 6, fontWeight: '600', textTransform: 'uppercase', letterSpacing: 0.5 },
  input: {
    backgroundColor: '#313244', color: '#cdd6f4', padding: 14, borderRadius: 8,
    marginBottom: 16, fontSize: 16,
  },
  iconInput: { textAlign: 'center', fontSize: 24, paddingVertical: 10 },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  createBtn: { backgroundColor: '#89b4fa', padding: 14, borderRadius: 8, alignItems: 'center', marginTop: 4 },
  createBtnDisabled: { opacity: 0.6 },
  createBtnText: { color: '#1e1e2e', fontWeight: '700', fontSize: 16 },
  cancelBtn: { alignItems: 'center', marginTop: 12 },
  cancelText: { color: '#6c7086', fontSize: 15 },
});

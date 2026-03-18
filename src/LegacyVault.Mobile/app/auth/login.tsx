import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, Alert
} from 'react-native';
import { router } from 'expo-router';
import * as SecureStore from 'expo-secure-store';
import { apiClient } from '../../src/api/client';
import { useDispatch } from 'react-redux';
import { AppDispatch } from '../../src/store';
import { setUser } from '../../src/store/authSlice';

export default function LoginScreen() {
  const dispatch = useDispatch<AppDispatch>();
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!username || !password) return;
    setLoading(true);
    try {
      const endpoint = mode === 'login' ? '/api/auth/login' : '/api/auth/register';
      const payload = mode === 'login'
        ? { username, password }
        : { username, email, password };

      const { data } = await apiClient.post(endpoint, payload);
      await SecureStore.setItemAsync('accessToken', data.accessToken);
      if (data.refreshToken) await SecureStore.setItemAsync('refreshToken', data.refreshToken);
      dispatch(setUser(data.user));
      router.replace('/app/');
    } catch (err: any) {
      Alert.alert('Error', err.response?.data || 'Login failed. Check server settings.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.logo}>🗄️</Text>
      <Text style={styles.title}>LegacyVault</Text>
      <Text style={styles.tagline}>Your personal knowledge vault</Text>

      <View style={styles.tabs}>
        <TouchableOpacity style={[styles.tab, mode === 'login' && styles.tabActive]}
          onPress={() => setMode('login')}>
          <Text style={[styles.tabText, mode === 'login' && styles.tabTextActive]}>Sign In</Text>
        </TouchableOpacity>
        <TouchableOpacity style={[styles.tab, mode === 'register' && styles.tabActive]}
          onPress={() => setMode('register')}>
          <Text style={[styles.tabText, mode === 'register' && styles.tabTextActive]}>Register</Text>
        </TouchableOpacity>
      </View>

      <TextInput style={styles.input} placeholder="Username" value={username}
        onChangeText={setUsername} autoCapitalize="none" autoCorrect={false} />
      {mode === 'register' && (
        <TextInput style={styles.input} placeholder="Email" value={email}
          onChangeText={setEmail} autoCapitalize="none" keyboardType="email-address" />
      )}
      <TextInput style={styles.input} placeholder="Password (min 8 chars)" value={password}
        onChangeText={setPassword} secureTextEntry />

      <TouchableOpacity style={styles.btn} onPress={handleSubmit} disabled={loading}>
        {loading ? <ActivityIndicator color="#1e1e2e" /> :
          <Text style={styles.btnText}>{mode === 'login' ? 'Sign In' : 'Create Account'}</Text>}
      </TouchableOpacity>

      <TouchableOpacity onPress={() => router.push('/settings')} style={styles.settingsLink}>
        <Text style={styles.settingsText}>⚙️ Server Settings</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#1e1e2e', alignItems: 'center', justifyContent: 'center', padding: 24 },
  logo: { fontSize: 48, marginBottom: 8 },
  title: { fontSize: 28, fontWeight: '700', color: '#cdd6f4', marginBottom: 4 },
  tagline: { fontSize: 14, color: '#6c7086', marginBottom: 32 },
  tabs: { flexDirection: 'row', backgroundColor: '#313244', borderRadius: 8, marginBottom: 20, width: '100%' },
  tab: { flex: 1, padding: 10, alignItems: 'center', borderRadius: 8 },
  tabActive: { backgroundColor: '#89b4fa' },
  tabText: { color: '#6c7086', fontWeight: '600', fontSize: 14 },
  tabTextActive: { color: '#1e1e2e' },
  input: {
    backgroundColor: '#313244', color: '#cdd6f4', padding: 14, borderRadius: 8,
    marginBottom: 12, width: '100%', fontSize: 16
  },
  btn: { backgroundColor: '#89b4fa', padding: 14, borderRadius: 8, width: '100%', alignItems: 'center', marginTop: 8 },
  btnText: { color: '#1e1e2e', fontWeight: '700', fontSize: 16 },
  settingsLink: { marginTop: 24 },
  settingsText: { color: '#6c7086', fontSize: 14 },
});

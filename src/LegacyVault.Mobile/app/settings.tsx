import React, { useEffect, useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import * as SecureStore from 'expo-secure-store';
import axios from 'axios';

const SERVER_URL_KEY = 'server_url';

export default function SettingsScreen() {
  const [url, setUrl] = useState('http://10.0.2.2:5000');
  const [testing, setTesting] = useState(false);

  useEffect(() => {
    SecureStore.getItemAsync(SERVER_URL_KEY).then((saved) => {
      if (saved) setUrl(saved);
    });
  }, []);

  const testConnection = async () => {
    setTesting(true);
    try {
      await axios.get(`${url}/api/auth/health`, { timeout: 5000 });
      await SecureStore.setItemAsync(SERVER_URL_KEY, url);
      Alert.alert('Connected!', 'Server is reachable. URL saved.');
    } catch {
      Alert.alert('Connection Failed', 'Could not reach the server. Check the URL and ensure LegacyVault is running.');
    } finally {
      setTesting(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.label}>API Server URL</Text>
      <Text style={styles.hint}>
        Emulator default: http://10.0.2.2:5000{'\n'}
        Physical device: http://&lt;your-pc-ip&gt;:5000
      </Text>
      <TextInput
        style={styles.input}
        value={url}
        onChangeText={setUrl}
        autoCapitalize="none"
        autoCorrect={false}
        keyboardType="url"
        placeholder="http://..."
        placeholderTextColor="#6c7086"
      />
      <TouchableOpacity style={styles.btn} onPress={testConnection} disabled={testing}>
        <Text style={styles.btnText}>{testing ? 'Testing…' : 'Test & Save'}</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#1e1e2e', padding: 24 },
  label: { fontSize: 16, fontWeight: '600', color: '#cdd6f4', marginBottom: 8 },
  hint: { fontSize: 13, color: '#6c7086', marginBottom: 16, lineHeight: 20 },
  input: {
    backgroundColor: '#313244', color: '#cdd6f4', padding: 14, borderRadius: 8,
    fontSize: 15, marginBottom: 16
  },
  btn: { backgroundColor: '#89b4fa', padding: 14, borderRadius: 8, alignItems: 'center' },
  btnText: { color: '#1e1e2e', fontWeight: '700', fontSize: 16 },
});

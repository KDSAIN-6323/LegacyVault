import { Stack } from 'expo-router';
import { Provider } from 'react-redux';
import { store } from '../src/store';

export default function RootLayout() {
  return (
    <Provider store={store}>
      <Stack screenOptions={{ headerShown: false }}>
        <Stack.Screen name="auth" />
        <Stack.Screen name="app" />
        <Stack.Screen name="settings" options={{ headerShown: true, title: 'Server Settings' }} />
      </Stack>
    </Provider>
  );
}

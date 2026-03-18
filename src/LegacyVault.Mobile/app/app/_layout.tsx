import { Stack } from 'expo-router';

export default function AppLayout() {
  return (
    <Stack
      screenOptions={{
        headerStyle: { backgroundColor: '#1e1e2e' },
        headerTintColor: '#cdd6f4',
        headerTitleStyle: { fontWeight: '600' },
        contentStyle: { backgroundColor: '#1e1e2e' },
      }}
    >
      <Stack.Screen name="index" options={{ headerShown: false }} />
      <Stack.Screen name="category/[id]" options={{ title: 'Pages' }} />
      <Stack.Screen name="category/[id]/page/[pageId]" options={{ title: 'Page' }} />
    </Stack>
  );
}

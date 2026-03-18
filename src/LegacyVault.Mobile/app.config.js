module.exports = {
  expo: {
    name: 'LegacyVault',
    slug: 'legacyvault',
    version: '1.0.0',
    scheme: 'legacyvault',
    orientation: 'portrait',
    icon: './assets/icon.png',
    splash: {
      image: './assets/splash.png',
      resizeMode: 'contain',
      backgroundColor: '#1e1e2e',
    },
    android: {
      adaptiveIcon: {
        foregroundImage: './assets/adaptive-icon.png',
        backgroundColor: '#1e1e2e',
      },
      package: 'com.legacyvault.app',
    },
    ios: {
      supportsTablet: true,
      bundleIdentifier: 'com.legacyvault.app',
    },
    plugins: [
      'expo-router',
      'expo-secure-store',
      [
        'expo-image-picker',
        {
          photosPermission: 'Legacy Vault needs access to your photo library to attach images to pages.',
          cameraPermission: 'Legacy Vault needs access to your camera to capture photos for pages.',
        },
      ],
      [
        'expo-media-library',
        {
          photosPermission: 'Legacy Vault needs access to your photo library to save images.',
          savePhotosPermission: 'Legacy Vault needs permission to save images to your device.',
          isAccessMediaLocationEnabled: true,
        },
      ],
    ],
    extra: {
      // Default: Android emulator points to host loopback via 10.0.2.2
      // Override with: API_BASE_URL=http://192.168.1.100:5000 npx expo start
      apiBaseUrl: process.env.API_BASE_URL || 'http://10.0.2.2:5000',
      eas: {
        projectId: 'YOUR_EAS_PROJECT_ID_HERE',
      },
    },
  },
};

import * as ImagePicker from 'expo-image-picker';
import * as MediaLibrary from 'expo-media-library';
import { Alert, Platform } from 'react-native';

export interface PickedMedia {
  uri: string;
  fileName: string;
  mimeType: string;
  fileSize?: number;
}

async function requestCameraPermission(): Promise<boolean> {
  const { status } = await ImagePicker.requestCameraPermissionsAsync();
  if (status !== 'granted') {
    Alert.alert('Permission required', 'Camera access is needed to take photos.');
    return false;
  }
  return true;
}

async function requestLibraryPermission(): Promise<boolean> {
  const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
  if (status !== 'granted') {
    Alert.alert('Permission required', 'Photo library access is needed to select images.');
    return false;
  }
  return true;
}

/** Open the device's native photo library picker. */
export async function pickFromLibrary(): Promise<PickedMedia | null> {
  if (!(await requestLibraryPermission())) return null;

  const result = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: ImagePicker.MediaTypeOptions.Images,
    quality: 0.9,
    allowsEditing: false,
    allowsMultipleSelection: false,
  });

  if (result.canceled || result.assets.length === 0) return null;

  const asset = result.assets[0];
  return {
    uri: asset.uri,
    fileName: asset.fileName ?? `photo_${Date.now()}.jpg`,
    mimeType: asset.mimeType ?? 'image/jpeg',
    fileSize: asset.fileSize,
  };
}

/** Open the device camera and capture a photo. */
export async function pickFromCamera(): Promise<PickedMedia | null> {
  if (!(await requestCameraPermission())) return null;

  const result = await ImagePicker.launchCameraAsync({
    mediaTypes: ImagePicker.MediaTypeOptions.Images,
    quality: 0.9,
    allowsEditing: false,
  });

  if (result.canceled || result.assets.length === 0) return null;

  const asset = result.assets[0];
  return {
    uri: asset.uri,
    fileName: asset.fileName ?? `photo_${Date.now()}.jpg`,
    mimeType: asset.mimeType ?? 'image/jpeg',
    fileSize: asset.fileSize,
  };
}

/**
 * Save an image URI to the device's default media library
 * (Camera Roll on iOS, Pictures on Android).
 */
export async function saveImageToDevice(uri: string): Promise<boolean> {
  const { status } = await MediaLibrary.requestPermissionsAsync();
  if (status !== 'granted') {
    Alert.alert('Permission required', 'Storage access is needed to save images.');
    return false;
  }

  try {
    const asset = await MediaLibrary.createAssetAsync(uri);

    // On Android, also place the asset in a named album so it appears in the
    // Pictures folder rather than the generic camera roll.
    if (Platform.OS === 'android') {
      let album = await MediaLibrary.getAlbumAsync('Legacy Vault');
      if (album) {
        await MediaLibrary.addAssetsToAlbumAsync([asset], album, false);
      } else {
        await MediaLibrary.createAlbumAsync('Legacy Vault', asset, false);
      }
    }

    return true;
  } catch {
    Alert.alert('Error', 'Could not save image to device.');
    return false;
  }
}

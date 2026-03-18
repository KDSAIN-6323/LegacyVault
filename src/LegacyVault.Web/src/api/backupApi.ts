import client from './axiosClient';

export interface BackupEntry {
  fileName: string;
  fileSizeBytes: number;
  createdAt: string;
}

export const backupApi = {
  list: () => client.get<BackupEntry[]>('/backup'),
  create: (password: string) => client.post<BackupEntry>('/backup', { password }),
  restore: (fileName: string, password: string) =>
    client.post<{ message: string }>(`/backup/restore/${encodeURIComponent(fileName)}`, { password }),
};

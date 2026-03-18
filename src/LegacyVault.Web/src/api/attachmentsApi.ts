import client from './axiosClient';
import { Attachment } from '../types';

export const attachmentsApi = {
  upload: (categoryId: string, pageId: string, file: File) => {
    const form = new FormData();
    form.append('file', file);
    return client.post<Attachment>(
      `/categories/${categoryId}/pages/${pageId}/attachments`,
      form,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
  },

  delete: (id: string) => client.delete(`/attachments/${id}`),
};

import client from './axiosClient';

export interface ReminderPage {
  pageId: string;
  categoryId: string;
  categoryName: string;
  categoryIcon: string;
  title: string;
  content: string; // JSON-encoded ReminderContent
}

export const remindersApi = {
  getAll: () => client.get<ReminderPage[]>('/reminders'),
};

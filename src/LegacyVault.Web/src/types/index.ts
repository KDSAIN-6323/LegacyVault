export type PageType = 'Recipe' | 'Quote' | 'Note' | 'HomeInventory' | 'Password' | 'Reminder' | 'ShoppingList';
export type CategoryType = 'General' | 'Vault';

export interface User {
  id: string;
  username: string;
  email: string;
}

export interface Category {
  id: string;
  type: CategoryType;
  name: string;
  icon: string;
  isEncrypted: boolean;
  encryptionSalt?: string;
  passwordHint?: string;
  isFavorite: boolean;
  pageCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface PageSummary {
  id: string;
  categoryId: string;
  type: PageType;
  title: string;
  isEncrypted: boolean;
  isFavorite: boolean;
  sortOrder: number;
  updatedAt: string;
}

export interface Page extends PageSummary {
  content: string;
  encryptionSalt?: string;
  encryptionIV?: string;
  createdAt: string;
  attachments: Attachment[];
}

export interface Attachment {
  id: string;
  fileName: string;
  mimeType: string;
  fileSize: number;
  url: string;
}

// Page content schemas
export interface RecipeContent {
  ingredients: string[];
  instructions: string[];
  servings: number;
  prepTime: string;
  cookTime: string;
  notes: string;
  locked?: boolean;
}

export interface QuoteContent {
  text: string;
  author: string;
  source: string;
  tags: string[];
  locked?: boolean;
}

export interface NoteContent {
  body: string;
}

export interface HomeInventoryContent {
  itemName: string;
  description: string;
  location: string;
  value: number;
  purchaseDate: string;
  serialNumber: string;
  warrantyExpiry: string;
  attachmentIds: string[];
}

export type ReminderTag = 'birthday' | 'anniversary' | 'holiday' | 'appointment' | 'custom';
export type ReminderRecurrence = 'once' | 'weekly' | 'monthly' | 'yearly';
export type NotifyUnit = 'hours' | 'days' | 'weeks';

export interface ReminderContent {
  date: string;                // ISO date YYYY-MM-DD (start date)
  endDate?: string;            // ISO date YYYY-MM-DD (end date for ranges, e.g. vacations)
  tag: ReminderTag;
  recurrence: ReminderRecurrence;
  recurrenceInterval?: number; // e.g. 2 = "every 2 weeks/months/years" (default 1)
  notes: string;
  notifyEnabled: boolean;
  notifyBefore: number;        // quantity, e.g. 1
  notifyUnit: NotifyUnit;      // hours | days | weeks
}

export interface PasswordContent {
  url: string;
  username: string;
  password: string;
  notes: string;
  totp?: string;
}

export interface ShoppingListItem {
  id: string;
  name: string;
  quantity: string;
  checked: boolean;
}

export interface ShoppingListContent {
  items: ShoppingListItem[];
  notes: string;
}

export type PageContent = RecipeContent | QuoteContent | NoteContent | HomeInventoryContent | PasswordContent | ReminderContent | ShoppingListContent;

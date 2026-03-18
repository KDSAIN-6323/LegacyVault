export type PageType =
  | 'Recipe'
  | 'Quote'
  | 'Note'
  | 'HomeInventory'
  | 'Password'
  | 'Reminder'
  | 'ShoppingList';

export interface User {
  id: string;
  username: string;
  email: string;
}

export interface Category {
  id: string;
  name: string;
  icon: string;
  isEncrypted: boolean;
  encryptionSalt?: string;
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
  sortOrder: number;
  updatedAt: string;
}

export interface Page extends PageSummary {
  content: string;
  encryptionIV?: string;
  attachments: Attachment[];
}

export interface Attachment {
  id: string;
  fileName: string;
  mimeType: string;
  fileSize: number;
  url: string;
}

// ── Content type interfaces ────────────────────────────────────

export interface RecipeContent {
  ingredients: string[];
  instructions: string[];
  servings?: number;
  prepTime?: string;
  cookTime?: string;
  notes?: string;
}

export interface QuoteContent {
  text: string;
  author?: string;
  source?: string;
  tags?: string[];
}

export interface NoteContent {
  body: string;
}

export interface HomeInventoryContent {
  itemName?: string;
  description?: string;
  location?: string;
  value?: number;
  purchaseDate?: string;
  serialNumber?: string;
  warrantyExpiry?: string;
}

export interface PasswordContent {
  url?: string;
  username?: string;
  password?: string;
  notes?: string;
  totp?: string;
}

export interface ReminderContent {
  date: string;                // ISO date YYYY-MM-DD (start date)
  endDate?: string;            // ISO date YYYY-MM-DD (end date for ranges, e.g. vacations)
  tag?: 'birthday' | 'anniversary' | 'holiday' | 'appointment' | 'custom';
  recurrence?: 'once' | 'weekly' | 'monthly' | 'yearly';
  recurrenceInterval?: number; // e.g. 2 = "every 2 weeks/months/years" (default 1)
  notes?: string;
  notifyEnabled?: boolean;
  notifyBefore?: number;
  notifyUnit?: 'hours' | 'days' | 'weeks';
}

export interface ShoppingListItem {
  text: string;
  checked: boolean;
}

export interface ShoppingListContent {
  items: ShoppingListItem[];
  notes?: string;
}

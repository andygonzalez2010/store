export interface IItem {
  id?: number;
  title?: string;
  description?: string;
  price?: number;
  count?: number;
  imageContentType?: string;
  image?: any;
}

export const defaultValue: Readonly<IItem> = {};

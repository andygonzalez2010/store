export interface IOrder {
  id?: number;
  quantity?: number;
  cartId?: number;
  itemTitle?: string;
  itemId?: number;
}

export const defaultValue: Readonly<IOrder> = {};

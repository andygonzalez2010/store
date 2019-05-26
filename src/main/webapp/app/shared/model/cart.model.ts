import { IOrder } from 'app/shared/model/order.model';

export interface ICart {
  id?: number;
  email?: string;
  orders?: IOrder[];
}

export const defaultValue: Readonly<ICart> = {};

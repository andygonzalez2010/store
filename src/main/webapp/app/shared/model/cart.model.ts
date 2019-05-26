import { Moment } from 'moment';
import { IOrder } from 'app/shared/model/order.model';

export interface ICart {
  id?: number;
  email?: string;
  closedAt?: Moment;
  orders?: IOrder[];
}

export const defaultValue: Readonly<ICart> = {};

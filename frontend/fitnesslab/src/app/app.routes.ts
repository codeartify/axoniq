import { Routes } from '@angular/router';
import { CustomerCreate } from './customers/customer-create';
import { CustomerDetail } from './customers/customer-detail';
import { CustomerList } from './customers/customer-list';

export const routes: Routes = [
  { path: 'customers', component: CustomerList },
  { path: 'customers/new', component: CustomerCreate },
  { path: 'customers/:id', component: CustomerDetail },
  { path: '', redirectTo: '/customers', pathMatch: 'full' }
];

import { Routes } from '@angular/router';
import { CustomerCreate } from './customers/customer-create';
import { CustomerDetail } from './customers/customer-detail';
import { CustomerList } from './customers/customer-list';
import { ProductCreate } from './products/product-create';
import { ProductDetail } from './products/product-detail';
import { ProductList } from './products/product-list';

export const routes: Routes = [
  { path: 'customers', component: CustomerList },
  { path: 'customers/new', component: CustomerCreate },
  { path: 'customers/:id', component: CustomerDetail },
  { path: 'products', component: ProductList },
  { path: 'products/new', component: ProductCreate },
  { path: 'products/:id', component: ProductDetail },
  { path: '', redirectTo: '/customers', pathMatch: 'full' }
];

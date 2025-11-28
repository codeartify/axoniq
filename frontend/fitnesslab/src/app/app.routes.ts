import { Routes } from '@angular/router';
import { CustomerCreate } from './customers/customer-create';
import { CustomerDetail } from './customers/customer-detail';
import { CustomerList } from './customers/customer-list';
import { ProductCreate } from './products/product-create';
import { ProductDetail } from './products/product-detail';
import { ProductList } from './products/product-list';
import { InvoiceList } from './invoices/invoice-list';
import { LoginComponent } from './auth/login.component';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: 'customers',
    component: CustomerList,
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'TRAINER'] }
  },
  {
    path: 'customers/new',
    component: CustomerCreate,
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'TRAINER'] }
  },
  {
    path: 'customers/:id',
    component: CustomerDetail,
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'TRAINER'] }
  },
  {
    path: 'products',
    component: ProductList,
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'TRAINER'] }
  },
  {
    path: 'products/new',
    component: ProductCreate,
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'TRAINER'] }
  },
  {
    path: 'products/:id',
    component: ProductDetail,
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'TRAINER'] }
  },
  {
    path: 'invoices',
    component: InvoiceList,
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'TRAINER'] }
  },
  { path: '', redirectTo: '/customers', pathMatch: 'full' }
];

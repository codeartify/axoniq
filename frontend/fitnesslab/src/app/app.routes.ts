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
  { path: 'unauthorized', component: LoginComponent },

  // ----------------------
  //   CUSTOMERS
  // ----------------------
  {
    path: 'customers',
    component: CustomerList,
    canActivate: [authGuard],
    data: { roles: ['customers.read'] } // list view
  },
  {
    path: 'customers/new',
    component: CustomerCreate,
    canActivate: [authGuard],
    data: { roles: ['customers.write'] } // create requires write
  },
  {
    path: 'customers/:id',
    component: CustomerDetail,
    canActivate: [authGuard],
    data: { roles: ['customers.read'] } // detail view; use button-level checks for edit
  },

  // ----------------------
  //   PRODUCTS
  // ----------------------
  {
    path: 'products',
    component: ProductList,
    canActivate: [authGuard],
    data: { roles: ['products.read'] } // Admin + Trainer
  },
  {
    path: 'products/new',
    component: ProductCreate,
    canActivate: [authGuard],
    data: { roles: ['products.write'] } // Admin only
  },
  {
    path: 'products/:id',
    component: ProductDetail,
    canActivate: [authGuard],
    data: { roles: ['products.read'] } // detail view
  },

  // ----------------------
  //   INVOICES
  // ----------------------
  {
    path: 'invoices',
    component: InvoiceList,
    canActivate: [authGuard],
    data: { roles: ['invoices.read'] }
  },

  { path: '', redirectTo: '/customers', pathMatch: 'full' }
];

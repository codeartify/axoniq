import {RedirectCommand, ResolveFn, Router, Routes} from '@angular/router';
import {CustomerCreate} from './customers/customer-create';
import {CustomerDetail} from './customers/customer-detail';
import {CustomerList} from './customers/customer-list';
import {ProductCreate} from './products/product-create';
import {ProductDetail} from './products/product-detail';
import {ProductList} from './products/product-list';
import {Login} from './auth/login';
import {authGuard} from './auth/auth.guard';
import {NotFound} from './not-found';
import {CustomersService, CustomerView, ProductView} from './generated-api';
import {inject} from '@angular/core';
import {InvoiceList} from './invoices/invoice-list';
import {Products} from './products/products';
import {catchError, of, timeout} from 'rxjs';
import {Error} from './error';
import {Dashboard} from './dashboard/dashboard';


export const resolveAllCustomers: ResolveFn<CustomerView[]> = () => inject(CustomersService).getAllCustomers()
export const resolveAllProducts: ResolveFn<ProductView[] | RedirectCommand> = () => {
  const router = inject(Router);
  return inject(Products).getAllProducts().pipe(
    timeout(2000),
    catchError((error) => {
      return of(new RedirectCommand(router.parseUrl('/error')))
    })
  );
};

export const routes: Routes = [
  {path: 'login', component: Login, data: {companyName: 'Fitness Management System'}},
  {path: 'unauthorized', component: Login},

  // ----------------------
  //   CUSTOMERS
  // ----------------------
  {
    path: 'customers',
    component: CustomerList,
    canActivate: [authGuard],
    data: {
      roles: ['customers.read']
    },
    resolve: {
      allCustomers: resolveAllCustomers
    }
  },
  {
    path: 'customers/new',
    component: CustomerCreate,
    canActivate: [authGuard],
    data: {roles: ['customers.write']}
  },
  {
    path: 'customers/:id',
    component: CustomerDetail,
    canActivate: [authGuard],
    data: {roles: ['customers.read']}
  },

  // ----------------------
  //   PRODUCTS
  // ----------------------
  {
    path: 'products',
    component: ProductList,
    canActivate: [authGuard],
    data: {roles: ['products.read']},
    resolve: {
      productsFromResolve: resolveAllProducts
    }
  },
  {
    path: 'products/new',
    component: ProductCreate,
    canActivate: [authGuard],
    data: {roles: ['products.write']} // Admin only
  },
  {
    path: 'products/:id',
    component: ProductDetail,
    canActivate: [authGuard],
    data: {roles: ['products.read']} // detail view
  },

  // ----------------------
  //   INVOICES
  // ----------------------
  {
    path: 'invoices',
    component: InvoiceList,
    canActivate: [authGuard],
    data: {roles: ['invoices.read']},
  },

  // ----------------------
  //   DASHBOARD
  // ----------------------
  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [authGuard],
    data: {roles: []}
  },

  {path: 'not-found', component: NotFound},
  {path: 'error', component: Error},
  {path: '', redirectTo: '/dashboard', pathMatch: 'full'},
  {path: '**', redirectTo: '/not-found'}

];

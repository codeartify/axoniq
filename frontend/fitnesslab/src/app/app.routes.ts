import {RedirectCommand, ResolveFn, Router, Routes} from '@angular/router';
import {authGuard} from './auth/auth.guard';
import {CustomersService, CustomerView, ProductView} from './generated-api';
import {inject} from '@angular/core';
import {Products} from './products/products';
import {catchError, of, timeout} from 'rxjs';


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

  // ----------------------
  //   CUSTOMERS
  // ----------------------
  {
    path: 'customers',
    loadComponent: () => import('./customers/customer-list').then(m => m.CustomerList),
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
    loadComponent: () => import('./customers/customer-create').then(m => m.CustomerCreate),
    canActivate: [authGuard],
    data: {roles: ['customers.write']}
  },
  {
    path: 'customers/:id',
    loadComponent: () => import('./customers/customer-detail').then(m => m.CustomerDetail),
    canActivate: [authGuard],
    data: {roles: ['customers.read']}
  },

  // ----------------------
  //   PRODUCTS
  // ----------------------
  {
    path: 'products',
    loadComponent: () => import('./products/product-list').then(m => m.ProductList),
    canActivate: [authGuard],
    data: {roles: ['products.read']},
    resolve: {
      productsFromResolve: resolveAllProducts
    }
  },
  {
    path: 'products/new',
    loadComponent: () => import('./products/product-create').then(m => m.ProductCreate),
    canActivate: [authGuard],
    data: {roles: ['products.write']} // Admin only
  },
  {
    path: 'products/:id',
    loadComponent: () => import('./products/product-detail').then(m => m.ProductDetail),
    canActivate: [authGuard],
    data: {roles: ['products.read']} // detail view
  },

  // ----------------------
  //   INVOICES
  // ----------------------
  {
    path: 'invoices',
    loadComponent: () => import('./invoices/invoice-list').then(m => m.InvoiceList),
    canActivate: [authGuard],
    data: {roles: ['invoices.read']},
  },

  // ----------------------
  //   NEWSLETTER
  // ----------------------
  {
    path: 'newsletter',
    loadComponent: () => import('./newsletter/newsletter-list').then(m => m.NewsletterList),
    canActivate: [authGuard],
    data: {roles: ['invoices.read']},
  },
  {
    path: 'newsletter/:id',
    loadComponent: () => import('./newsletter/newsletter-editor').then(m => m.NewsletterEditor),
    canActivate: [authGuard],
    data: {roles: ['invoices.read']},
  },

  // ----------------------
  //   DASHBOARD
  // ----------------------
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard').then(m => m.Dashboard),
    canActivate: [authGuard],
    data: {roles: []}
  },

  {path: 'not-found', loadComponent: () => import('./not-found').then(m => m.NotFound)},
  {path: 'error', loadComponent: () => import('./error').then(m => m.Error)},
  {path: 'login', loadComponent: () => import('./auth/login').then(m => m.Login), data: {companyName: 'Fitness Management System'}},
  {path: 'unauthorized', loadComponent: () => import('./auth/login').then(m => m.Login)},
  {path: '', redirectTo: '/dashboard', pathMatch: 'full'},
  {path: '**', redirectTo: '/not-found'}

];

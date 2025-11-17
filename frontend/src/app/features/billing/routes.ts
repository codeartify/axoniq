import { Routes } from '@angular/router';

export const BILLING_ROUTES: Routes = [
  {
    path: 'invoices',
    loadComponent: () => import('./invoices/invoice-list.component').then(m => m.InvoiceListComponent)
  }
];

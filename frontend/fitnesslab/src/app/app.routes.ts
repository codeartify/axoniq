import { Routes } from '@angular/router';
import { CustomerCreateComponent } from './customers/customer-create.component';
import { CustomerDetailComponent } from './customers/customer-detail.component';

export const routes: Routes = [
  { path: 'customers/new', component: CustomerCreateComponent },
  { path: 'customers/:id', component: CustomerDetailComponent },
  { path: '', redirectTo: '/customers/new', pathMatch: 'full' }
];

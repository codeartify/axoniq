import { Routes } from '@angular/router';
import { CustomerCreateComponent } from './customers/customer-create.component';
import { CustomerDetailComponent } from './customers/customer-detail.component';
import { CustomerListComponent } from './customers/customer-list.component';

export const routes: Routes = [
  { path: 'customers', component: CustomerListComponent },
  { path: 'customers/new', component: CustomerCreateComponent },
  { path: 'customers/:id', component: CustomerDetailComponent },
  { path: '', redirectTo: '/customers', pathMatch: 'full' }
];

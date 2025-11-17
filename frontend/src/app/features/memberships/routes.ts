import { Routes } from '@angular/router';

export const MEMBERSHIP_ROUTES: Routes = [
  {
    path: 'sign-up',
    loadComponent: () => import('./sign-up/sign-up.component').then(m => m.SignUpComponent)
  }
];

import {Component, computed, inject} from '@angular/core';
import {Router} from '@angular/router';
import {CommonModule} from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';
import AuthService from '../auth/auth.service';

@Component({
  selector: 'gym-dashboard',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  template: `
    <div class="min-h-screen bg-gray-50 p-8">
      <div class="max-w-7xl mx-auto">
        <h1 class="text-3xl font-bold text-gray-900 mb-8">
          {{ 'dashboard.title' | translate }}
        </h1>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          @if (canViewCustomers()) {
            <div
              class="bg-white rounded-lg shadow p-6 cursor-pointer hover:shadow-lg transition-shadow"
              (click)="navigateTo('/customers')">
              <div class="flex items-center mb-4">
                <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path>
                  </svg>
                </div>
              </div>
              <h2 class="text-xl font-semibold text-gray-900 mb-2">
                {{ 'nav.customers' | translate }}
              </h2>
            </div>
          }

          @if (canViewProducts()) {
            <div
              class="bg-white rounded-lg shadow p-6 cursor-pointer hover:shadow-lg transition-shadow"
              (click)="navigateTo('/products')">
              <div class="flex items-center mb-4">
                <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                  <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path>
                  </svg>
                </div>
              </div>
              <h2 class="text-xl font-semibold text-gray-900 mb-2">
                {{ 'nav.products' | translate }}
              </h2>
            </div>
          }

          @if (canViewInvoices()) {
            <div
              class="bg-white rounded-lg shadow p-6 cursor-pointer hover:shadow-lg transition-shadow"
              (click)="navigateTo('/invoices')">
              <div class="flex items-center mb-4">
                <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                  <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                  </svg>
                </div>
              </div>
              <h2 class="text-xl font-semibold text-gray-900 mb-2">
                {{ 'nav.invoices' | translate }}
              </h2>
            </div>
          }
        </div>

        <div class="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-blue-900 mb-2">
            {{ 'dashboard.commonUseCases' | translate }}
          </h3>
        </div>
      </div>
    </div>
  `
})
export class Dashboard {
  private router = inject(Router);
  private authService = inject(AuthService);

  canViewCustomers = computed(() => this.authService.hasRole('customers.read'));
  canViewProducts = computed(() => this.authService.hasRole('products.read'));
  canViewInvoices = computed(() => this.authService.hasRole('invoices.read'));


  navigateTo(path: string) {
    this.router.navigate([path]);
  }
}

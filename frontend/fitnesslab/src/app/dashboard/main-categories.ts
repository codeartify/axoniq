import {Component, computed, inject, Signal, signal} from '@angular/core';
import {CustomersIcon} from '../shared/ui-elements/customers-icon';
import {TranslatePipe} from '@ngx-translate/core';
import {Router} from '@angular/router';
import AuthService from '../auth/auth.service';
import {NgComponentOutlet} from '@angular/common';
import {ProductBox} from '../shared/ui-elements/product-box';
import {BillsIcon} from '../shared/ui-elements/bills-icon';

interface MainCategory {
  nameTranslationKey: string;
  route: string;
  iconComponent: any;
  canView: Signal<boolean>;
}

@Component({
  selector: 'gym-main-categories',
  imports: [
    TranslatePipe,
    NgComponentOutlet
  ],
  template: `
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
      @for (category of mainCategories(); track category) {
        @if (category.canView()) {
          <div
            class="bg-slate-800 rounded-lg shadow-lg p-5 sm:p-6 cursor-pointer hover:shadow-xl hover:bg-slate-750 transition-all active:scale-95 border border-slate-700"
            (click)="navigateTo(category.route)">
            <div class="flex items-center mb-3 sm:mb-4">
              <ng-container [ngComponentOutlet]="category.iconComponent"/>
            </div>
            <h2 class="text-lg sm:text-xl font-semibold text-slate-50 mb-2">
              {{ category.nameTranslationKey | translate }}
            </h2>
          </div>
        }
      }
    </div>`,
  styles: ``,
})
export class MainCategories {
  private router = inject(Router);
  private authService = inject(AuthService);

  canViewCustomers = computed(() => this.authService.hasRole('customers.read'));
  canViewProducts = computed(() => this.authService.hasRole('products.read'));
  canViewInvoices = computed(() => this.authService.hasRole('invoices.read'));

  mainCategories = signal<MainCategory[]>([
    {
      nameTranslationKey: "nav.customers",
      route: "customers",
      iconComponent: CustomersIcon,
      canView: this.canViewCustomers
    },
    {
      nameTranslationKey: "nav.products",
      route: "products",
      iconComponent: ProductBox,
      canView: this.canViewProducts
    },
    {
      nameTranslationKey: "nav.invoices",
      route: 'invoices',
      iconComponent: BillsIcon,
      canView: this.canViewInvoices
    }
  ]);


  navigateTo(path: string) {
    this.router.navigate([path]);
  }
}

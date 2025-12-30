import {Component, inject, signal} from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';
import {Router} from '@angular/router';

interface UseCaseTile {
  title: string;
  route: string;
}

@Component({
  selector: 'gym-common-use-cases',
  imports: [
    TranslatePipe
  ],
  template: `<div class="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-6">
    <h3 class="text-lg font-semibold text-blue-900 mb-4">
      {{ 'dashboard.commonUseCases' | translate }}
    </h3>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      @for (useCase of commonUseCases(); track useCase.route) {
        <div
          class="bg-white rounded-lg shadow p-4 cursor-pointer hover:shadow-md transition-shadow border border-blue-100"
          (click)="navigateTo(useCase.route)">
          <h4 class="text-md font-semibold text-gray-900">
            {{ useCase.title | translate }}
          </h4>
        </div>
      }
    </div>
  </div>
  `,
})
export class CommonUseCases {
  private router = inject(Router);

  commonUseCases = signal<UseCaseTile[]>([
    {title: 'commonUseCases.newMembership', route: '/membership/new'},
    {title: 'Add new customer', route: '/customers/new'},
    {title: 'View invoices', route: '/invoices'}
  ]);

  navigateTo(path: string) {
    this.router.navigate([path]);
  }
}

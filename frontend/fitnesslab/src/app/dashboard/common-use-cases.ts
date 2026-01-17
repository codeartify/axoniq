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
  template: `<div class="mt-6 sm:mt-8 bg-slate-800 border border-slate-700 rounded-lg p-4 sm:p-6">
    <h3 class="text-base sm:text-lg font-semibold text-blue-400 mb-3 sm:mb-4">
      {{ 'dashboard.commonUseCases' | translate }}
    </h3>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
      @for (useCase of commonUseCases(); track useCase.route) {
        <button class="bg-slate-700 rounded-lg shadow-lg p-4 sm:p-6 cursor-pointer hover:shadow-xl hover:bg-slate-600 transition-all border border-slate-600 flex items-center gap-3 sm:gap-4 active:scale-95"
          (click)="navigateTo(useCase.route)">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6 sm:w-8 sm:h-8 text-blue-400 flex-shrink-0">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 9h3.75M15 12h3.75M15 15h3.75M4.5 19.5h15a2.25 2.25 0 0 0 2.25-2.25V6.75A2.25 2.25 0 0 0 19.5 4.5h-15a2.25 2.25 0 0 0-2.25 2.25v10.5A2.25 2.25 0 0 0 4.5 19.5Zm6-10.125a1.875 1.875 0 1 1-3.75 0 1.875 1.875 0 0 1 3.75 0Zm1.294 6.336a6.721 6.721 0 0 1-3.17.789 6.721 6.721 0 0 1-3.168-.789 3.376 3.376 0 0 1 6.338 0Z" />
          </svg>
          <h4 class="text-base sm:text-lg font-semibold text-slate-50">
            {{ useCase.title | translate }}
          </h4>
        </button>
      }
    </div>
  </div>
  `,
})
export class CommonUseCases {
  private router = inject(Router);

  commonUseCases = signal<UseCaseTile[]>([
    {title: 'dashboard.commonUseCases.createMembership', route: '/membership/new'},
  ]);

  navigateTo(path: string) {
    this.router.navigate([path]);
  }
}

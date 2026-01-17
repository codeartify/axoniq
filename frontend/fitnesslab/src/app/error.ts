import {Component, inject} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'gym-error',
  imports: [RouterLink, TranslatePipe],
  template: `
    <div class="min-h-[calc(100vh-4rem)] bg-slate-900 flex items-center justify-center px-4 py-12">
      <div class="w-full max-w-xl">
        <div class="bg-slate-800 border border-slate-700 rounded-2xl shadow-sm overflow-hidden">
          <div class="px-6 py-10 sm:px-10">
            <div class="flex items-start gap-4">
              <div class="shrink-0">
                <div class="h-12 w-12 rounded-xl bg-red-50 flex items-center justify-center">
                  <svg viewBox="0 0 24 24" fill="none" class="h-6 w-6 text-red-400" aria-hidden="true">
                    <path
                      d="M12 9v4m0 4h.01M10.29 3.86l-7.4 12.82A2 2 0 0 0 4.6 20h14.8a2 2 0 0 0 1.71-3.32l-7.4-12.82a2 2 0 0 0-3.42 0Z"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                </div>
              </div>

              <div class="min-w-0">
                <h1 class="text-xl sm:text-2xl font-bold text-slate-50">
                  {{ 'error.title' | translate }}
                </h1>
                <p class="mt-2 text-sm text-slate-400">
                  {{'error.description' | translate}}
                </p>

                <div class="mt-6 flex flex-col sm:flex-row gap-3">

                  <button
                    type="button"
                    (click)="goBack()"
                    class="inline-flex items-center justify-center rounded-lg bg-slate-800 px-4 py-2.5 text-sm font-semibold text-slate-50 shadow-sm ring-1 ring-inset ring-slate-600 hover:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:ring-offset-2"
                  >
                    {{ 'error.goBack' | translate}}
                  </button>

                  <a
                    routerLink="/customers"
                    class="inline-flex items-center justify-center rounded-lg px-4 py-2.5 text-sm font-semibold text-slate-300 hover:text-slate-50"
                  >
                    {{ 'error.toDashboard' | translate}}
                  </a>
                </div>

                <div class="mt-6 rounded-lg border border-slate-700 bg-slate-900 px-4 py-3">
                  <p class="text-xs text-slate-400">
                    {{ 'error.tip' | translate }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div class="px-6 py-4 sm:px-10 border-t border-slate-700 bg-slate-800">
            <p class="text-xs text-slate-400">
              {{ 'error.errorPage' | translate}}
            </p>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class Error {


  goBack(): void {
    window.history.back();
  }
}

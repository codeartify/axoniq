import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'gym-not-found',
  imports: [RouterLink, TranslatePipe],
  template: `
    <section class="min-h-[60vh] flex items-center justify-center px-6 py-16">
      <div class="w-full max-w-2xl text-center">

        <h1 class="mt-3 text-3xl sm:text-4xl font-bold text-slate-50">
          {{ 'notFound.title' | translate }}
        </h1>

        <p class="mt-4 text-slate-400">
          {{ 'notFound.description' | translate }}
        </p>

        <div class="mt-8 flex flex-col sm:flex-row gap-3 justify-center">
          <a
            routerLink="/"
            class="inline-flex items-center justify-center rounded-lg bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-blue-700 transition"
          >
            {{ 'notFound.toDashboard' | translate }}
          </a>

        </div>
      </div>
    </section>
  `,
})
export class NotFound {
}

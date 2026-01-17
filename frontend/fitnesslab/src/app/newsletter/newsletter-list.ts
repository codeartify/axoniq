import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { NewsletterService } from './newsletter.service';

@Component({
  selector: 'gym-newsletter-list',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  template: `
    <div class="p-3 sm:p-5 max-w-7xl mx-auto">
      <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3 mb-5">
        <h2 class="text-2xl sm:text-3xl font-bold text-slate-50">{{ 'newsletter.title' | translate }}</h2>
        <button
          (click)="createNewsletter()"
          class="w-full sm:w-auto px-4 py-2 bg-blue-500 text-white rounded border-none cursor-pointer text-sm font-medium hover:bg-blue-600 transition-colors"
        >
          {{ 'newsletter.createNewsletter' | translate }}
        </button>
      </div>

      <div class="bg-slate-800 shadow-md rounded-lg p-4 sm:p-6">
        @if (newsletters().length === 0) {
          <div class="text-center py-20">
            <p class="text-slate-400 text-lg mb-4">{{ 'newsletter.noNewsletters' | translate }}</p>
            <button
              (click)="createNewsletter()"
              class="px-4 py-2 bg-blue-500 text-white rounded border-none cursor-pointer text-sm font-medium hover:bg-blue-600 transition-colors"
            >
              {{ 'newsletter.createFirstNewsletter' | translate }}
            </button>
          </div>
        } @else {
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            @for (newsletter of newsletters(); track newsletter.id) {
              <div class="bg-slate-700 rounded-lg p-4 border border-slate-600 hover:border-slate-500 transition-colors">
                <h3 class="text-lg font-semibold text-slate-50 mb-2">{{ newsletter.title }}</h3>
                <p class="text-sm text-slate-400 mb-4">
                  {{ 'newsletter.lastUpdated' | translate }}: {{ newsletter.updatedAt | date:'short' }}
                </p>
                <div class="flex gap-2">
                  <button
                    (click)="editNewsletter(newsletter.id)"
                    class="flex-1 px-3 py-2 bg-blue-500 text-white rounded text-sm hover:bg-blue-600 transition-colors"
                  >
                    {{ 'common.edit' | translate }}
                  </button>
                  <button
                    (click)="deleteNewsletter(newsletter.id)"
                    class="px-3 py-2 bg-red-600 text-white rounded text-sm hover:bg-red-700 transition-colors"
                  >
                    {{ 'common.delete' | translate }}
                  </button>
                </div>
              </div>
            }
          </div>
        }
      </div>
    </div>
  `
})
export class NewsletterList {
  private router = inject(Router);
  private newsletterService = inject(NewsletterService);

  newsletters = this.newsletterService.getNewsletters();

  createNewsletter(): void {
    this.router.navigate(['/newsletter/new']);
  }

  editNewsletter(id: string): void {
    this.router.navigate(['/newsletter', id]);
  }

  deleteNewsletter(id: string): void {
    if (confirm('Are you sure you want to delete this newsletter?')) {
      this.newsletterService.deleteNewsletter(id);
    }
  }
}

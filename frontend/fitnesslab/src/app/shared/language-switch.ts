import {Component, computed, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';
import {TranslationService} from './translation.service';

type Language = { code: string; label: string };

@Component({
  selector: 'gym-language-switch',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  template: `
    <div class="relative inline-block">
      <button
        (click)="toggleDropdown()"
        class="flex items-center gap-2 px-3 py-2 text-gray-700 hover:text-blue-600 transition-colors bg-white border border-gray-300 rounded-md hover:bg-gray-50"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129"></path>
        </svg>
        <span class="font-medium">{{ currentLanguage().label | translate }}</span>
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
        </svg>
      </button>

      @if (isOpen()) {
        <div class="absolute right-0 mt-2 w-32 bg-white border border-gray-300 rounded-md shadow-lg z-50">
          @for (lang of languages; track lang.code) {
            <button
              (click)="changeLanguage(lang.code)"
              class="w-full text-left px-4 py-2 text-sm hover:bg-blue-50 transition-colors flex items-center justify-between"
              [class.bg-blue-100]="currentLanguage().code === lang.code"
              [class.font-semibold]="currentLanguage().code === lang.code"
            >
              <span> {{ lang.label | translate }}</span>
              @if (currentLanguage().code === lang.code) {
                <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd"
                        d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                        clip-rule="evenodd"></path>
                </svg>
              }
            </button>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    :host {
      display: inline-block;
    }
  `]
})
export class LanguageSwitch {

  isOpen = signal(false);
  languages = [
    {code: 'en', label: 'language.english'},
    {code: 'de', label: 'language.german'}
  ];

  private translationService = inject(TranslationService);

  private language = () => {
    return {
      code: this.translationService.getCurrentLanguage(),
      label: this.languageFrom(this.languages)
    }
  };
  currentLanguage = signal<Language>(this.language());


  private languageFrom(languages: Language[]) {
    const lang = languages.find(l => l.code === this.translationService.getCurrentLanguage());
    return lang ? lang.label : 'Language';
  }

  toggleDropdown(): void {
    this.isOpen.set(!this.isOpen());
  }

  changeLanguage(lang: string): void {
    this.translationService.changeLanguage(lang);
    this.currentLanguage.set(this.language());
    this.isOpen.set(false);
  }

}

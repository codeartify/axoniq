import {inject, Injectable} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Location} from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  private readonly SUPPORTED_LANGUAGES = ['en', 'de'];
  private readonly DEFAULT_LANGUAGE = 'de';

  private translateService = inject(TranslateService)
  private location = inject(Location)

  constructor() {
    this.initializeLanguage();
  }

  private initializeLanguage(): void {
    // Get language from URL query parameter
    const urlParams = new URLSearchParams(window.location.search);
    const langParam = urlParams.get('lang');

    // Determine the language to use
    const language = this.isValidLanguage(langParam)
      ? langParam
      : this.DEFAULT_LANGUAGE;

    // Set the language
    this.translateService.setDefaultLang(this.DEFAULT_LANGUAGE);
    this.translateService.use(language!);
  }

  private isValidLanguage(lang: string | null): boolean {
    return lang !== null && this.SUPPORTED_LANGUAGES.includes(lang);
  }

  getCurrentLanguage(): string {
    return this.translateService.currentLang || this.DEFAULT_LANGUAGE;
  }

  getSupportedLanguages(): string[] {
    return [...this.SUPPORTED_LANGUAGES];
  }

  changeLanguage(lang: string): void {
    if (!this.isValidLanguage(lang)) {
      console.warn(`Language '${lang}' is not supported. Using default language.`);
      lang = this.DEFAULT_LANGUAGE;
    }

    // Update the translation service
    this.translateService.use(lang);

    // Update the URL with the new language parameter
    this.updateUrlParameter('lang', lang);
  }

  private updateUrlParameter(key: string, value: string): void {
    const url = new URL(window.location.href);
    url.searchParams.set(key, value);

    // Update the URL without reloading the page
    this.location.replaceState(url.pathname + url.search);
  }

  translate(key: string, params?: any): string {
    return this.translateService.instant(key, params);
  }
}

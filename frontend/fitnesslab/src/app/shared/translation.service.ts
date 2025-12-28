import {inject, Injectable} from '@angular/core';

import {TranslateService} from '@ngx-translate/core';
import {Location} from '@angular/common';

export interface Language { code: string; label: string }

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  SUPPORTED_LANGUAGES = [
    {code: 'en', label: 'language.english'},
    {code: 'de', label: 'language.german'}
  ];

  private readonly DEFAULT_LANGUAGE_CODE = 'de';


  language = () => {
    return {
      code: this.getCurrentLanguage(),
      label: this.languageFrom(this.SUPPORTED_LANGUAGES)
    }
  };

  private languageFrom(languages: Language[]) {
    const lang = languages.find(l => l.code === this.getCurrentLanguage());
    return lang ? lang.label : 'Language';
  }
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
      : this.DEFAULT_LANGUAGE_CODE;

    // Set the language
    this.translateService.setDefaultLang(this.DEFAULT_LANGUAGE_CODE);
    this.translateService.use(language!);
  }

  private isValidLanguage(lang: string | null): boolean {
    return lang !== null && this.SUPPORTED_LANGUAGES.map(l => l.code).includes(lang);
  }

  getCurrentLanguage(): string {
    return this.translateService.currentLang || this.DEFAULT_LANGUAGE_CODE;
  }

  getSupportedLanguages(): Language[] {
    return [...this.SUPPORTED_LANGUAGES];
  }

  changeLanguage(lang: Language): void {
    let code = lang.code

    if (!this.isValidLanguage(code)) {
      console.warn(`Language '${lang}' is not supported. Using default language.`);
      code = this.DEFAULT_LANGUAGE_CODE;
    }

    // Update the translation service
    this.translateService.use(code);

    // Update the URL with the new language parameter
    this.updateUrlParameter('lang', code);
  }

  private updateUrlParameter(key: string, value: string): void {
    const url = new URL(window.location.href);
    url.searchParams.set(key, value);

    // Update the URL without reloading the page
    this.location.replaceState(url.pathname + url.search);
  }

}

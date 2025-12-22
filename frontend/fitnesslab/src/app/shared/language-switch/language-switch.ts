import {Component, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';
import {Language, TranslationService} from '../translation.service';
import {LanguageSelector} from './language-selector';
import {LanguageIcon} from './language-icon';
import {DropdownArrow} from '../ui-elements/dropdown-arrow';


@Component({
  selector: 'gym-language-switch',
  standalone: true,
  imports: [CommonModule, TranslateModule, LanguageSelector, LanguageIcon, DropdownArrow],
  styles: [`
    :host {
      display: inline-block;
    }
  `],
  template: `
    <div class="relative inline-block">
      <button
        (click)="toggleDropdown()"
        class="flex items-center gap-2 px-3 py-2 text-gray-700 hover:text-blue-600 transition-colors bg-white border border-gray-300 rounded-md hover:bg-gray-50">
        <gym-language-icon/>
        <span class="font-medium">{{ currentLanguage().label | translate }}</span>
        <gym-dropdown-arrow/>
      </button>

      @if (isOpen()) {
        <gym-language-selector
          [languages]="languages"
          [currentLanguage]="currentLanguage()"
          (languageChanged)="changeLanguage($event)"
        />
      }
    </div>
  `
})
export class LanguageSwitch {
  private translationService = inject(TranslationService);
  isOpen = signal(false);
  readonly languages = this.translationService.SUPPORTED_LANGUAGES
  currentLanguage = this.translationService.language;


  toggleDropdown() {
    this.isOpen.set(!this.isOpen());
  }

  changeLanguage(lang: Language) {
    this.translationService.changeLanguage(lang);
    this.isOpen.set(false);
  }
}

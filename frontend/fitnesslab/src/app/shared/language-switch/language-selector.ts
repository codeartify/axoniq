import {Component, input, output} from '@angular/core';
import {LanguageSelection} from './language-selection';
import {Language} from '../translation.service';

@Component({
  selector: 'gym-language-selector',
  imports: [
    LanguageSelection
  ],
  template: `
    <div class="absolute right-0 mt-2 w-32 bg-white border border-gray-300 rounded-md shadow-lg z-50">
      @for (languageSelection of languages(); track languageSelection.code) {
        <gym-language-selection
          [languageSelection]="languageSelection"
          [selectedLanguage]="currentLanguage()"
          (languageChanged)="changeLanguage($event)" />
      }
    </div>
  `,
  styles: ``,
})
export class LanguageSelector {
  languages = input.required<Language[]>();
  currentLanguage = input.required<Language>();
  languageChanged = output<Language>();

  changeLanguage(languageCode: Language): void {
    this.languageChanged.emit(languageCode);
  }
}

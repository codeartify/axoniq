import {Component, input, output} from '@angular/core';
import { TranslatePipe} from '@ngx-translate/core';
import {Language} from '../translation.service';
import {Tick} from '../ui-elements/tick';

@Component({
  selector: 'gym-language-selection',
  imports: [
    TranslatePipe,
    Tick
  ],
  template: `
    @let languageLabel = languageSelection().label;
    @let languageCode = languageSelection().code;
    @let selectedLang = selectedLanguage();
    @let isLangueSelected = selectedLang.code === languageCode;

    <button
      (click)="changeLanguage()"
      class="w-full text-left px-4 py-2 text-sm hover:bg-blue-50 transition-colors flex items-center justify-between"
      [class.bg-blue-100]="isLangueSelected"
      [class.font-semibold]="isLangueSelected"
    >
      <span> {{ languageLabel | translate }}</span>

      @if (isLangueSelected) {
        <gym-tick/>
      }

    </button>
  `,
})
export class LanguageSelection {
  languageSelection = input.required<Language>()
  selectedLanguage = input.required<Language>()

  languageChanged = output<Language>()

  changeLanguage(): void {
    this.languageChanged.emit(this.languageSelection());
  }

}

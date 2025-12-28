import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';
import {CommonUseCases} from './common-use-cases';
import {MainCategories} from './main-categories';


@Component({
  selector: 'gym-dashboard',
  standalone: true,
  imports: [CommonModule, TranslateModule, CommonUseCases, MainCategories],
  template: `
    <div class="min-h-screen bg-gray-50 p-8">
      <div class="max-w-7xl mx-auto">
        <h1 class="text-3xl font-bold text-gray-900 mb-8">
          {{ 'dashboard.title' | translate }}
        </h1>

        <gym-main-categories/>
        <gym-common-use-cases/>
      </div>
    </div>
  `
})
export class Dashboard {
}

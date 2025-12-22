import {Component, inject, signal} from '@angular/core';
import {Navigation} from './navigation';
import {LanguageSwitch} from './shared/language-switch/language-switch';
import {UserProfileMenu} from './auth/user-profile/user-profile-menu';
import AuthService from './auth/auth.service';

@Component({
  selector: 'gym-header',
  imports: [
    Navigation,
    LanguageSwitch,
    UserProfileMenu
  ],
  template: `
    <header class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">

        <h1 class="text-2xl font-bold text-gray-900">{{ title() }}</h1>

        @if (isLoggedIn()) {
          <div class="flex items-center gap-6">
            <gym-navigation/>
            <gym-language-switch/>
            <gym-user-profile-menu/>
          </div>
        }

      </div>
    </header>`,
})
export class Header {
  title = signal('Fitness Management System');
  isLoggedIn = inject(AuthService).isLoggedIn;
}

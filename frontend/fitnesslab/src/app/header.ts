import {Component, input, signal} from '@angular/core';
import {Navigation} from './navigation';
import {LanguageSwitch} from './shared/language-switch/language-switch';
import {UserProfileMenu} from './auth/user-profile/user-profile-menu';

@Component({
  selector: 'gym-header',
  imports: [
    Navigation,
    LanguageSwitch,
    UserProfileMenu
  ],
  template: `
    <header class="bg-slate-800 shadow-lg sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 py-4">
        <!-- Mobile header -->
        <div class="flex justify-between items-center lg:hidden">
          <h1 class="text-xl font-bold text-slate-50 truncate">{{ title() }}</h1>
          <button
            (click)="toggleMobileMenu()"
            class="p-2 text-slate-300 hover:text-slate-50 focus:outline-none"
            aria-label="Toggle menu"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              @if (!mobileMenuOpen()) {
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
              } @else {
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              }
            </svg>
          </button>
        </div>

        <!-- Desktop header -->
        <div class="hidden lg:flex justify-between items-center">
          <h1 class="text-2xl font-bold text-slate-50">{{ title() }}</h1>
          <div class="flex items-center gap-6">
            <gym-navigation/>
            <gym-language-switch/>
            <gym-user-profile-menu/>
          </div>
        </div>

        <!-- Mobile menu -->
        @if (mobileMenuOpen()) {
          <div class="lg:hidden mt-4 pb-4 border-t border-slate-700 pt-4">
            <gym-navigation [mobile]="true"/>
            <div class="flex items-center gap-4 mt-4 pt-4 border-t border-slate-700">
              <gym-language-switch/>
              <gym-user-profile-menu/>
            </div>
          </div>
        }
      </div>
    </header>`,
})
export class Header {
  title = input.required<string>();
  mobileMenuOpen = signal(false);

  toggleMobileMenu(): void {
    this.mobileMenuOpen.update(open => !open);
  }
}

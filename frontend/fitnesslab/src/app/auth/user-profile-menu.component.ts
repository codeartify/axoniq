import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import AuthService, { UserProfile } from './auth.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-user-profile-menu',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  template: `
    @if (userProfile) {
      <div class="relative">
        <!-- Profile Image Button -->
        <button
          (click)="toggleMenu()"
          class="flex items-center justify-center w-10 h-10 rounded-full bg-blue-600 text-white font-semibold hover:bg-blue-700 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
          @if (userProfile.picture) {
            <img
              [src]="userProfile.picture"
              [alt]="userProfile.firstName + ' ' + userProfile.lastName"
              class="w-10 h-10 rounded-full object-cover"
            />
          }
          @if (!userProfile.picture) {
            <span>{{ getInitials() }}</span>
          }
        </button>

        <!-- Dropdown Menu -->
        @if (isOpen()) {
          <div
            class="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-xl border border-gray-200 py-2 z-50"
          >
            <!-- User Info -->
            <div class="px-4 py-3 border-b border-gray-200">
              <p class="text-sm font-semibold text-gray-900">
                {{ userProfile.firstName }} {{ userProfile.lastName }}
              </p>
              <p class="text-xs text-gray-500 mt-1">{{ userProfile.email }}</p>
              <div class="flex gap-1 mt-2">
                @for (role of userProfile.roles; track role) {
                  <span
                    class="inline-block px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded"
                  >
                    {{ role }}
                  </span>
                }
              </div>
            </div>

            <!-- Logout Button -->
            <button
              (click)="logout()"
              class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
            >
              {{ 'auth.logout' | translate }}
            </button>
          </div>
        }

        <!-- Backdrop -->
        @if (isOpen()) {
          <div
            (click)="closeMenu()"
            (keydown.enter)="closeMenu()"
            (keydown.escape)="closeMenu()"
            tabindex="0"
            role="button"
            aria-label="Close menu"
            class="fixed inset-0 z-40"></div>
        }
      </div>
    }
  `,
})
export class UserProfileMenuComponent {
  private authService = inject(AuthService);

  isOpen = signal(false);
  userProfile: UserProfile | null = null;

  constructor() {
    this.authService.userProfile$.subscribe((profile) => {
      this.userProfile = profile;
    });
  }

  toggleMenu(): void {
    this.isOpen.update((value) => !value);
  }

  closeMenu(): void {
    this.isOpen.set(false);
  }

  logout(): void {
    this.closeMenu();
    this.authService.logout();
  }

  getInitials(): string {
    if (!this.userProfile) return '';
    const first = this.userProfile.firstName?.charAt(0) || '';
    const last = this.userProfile.lastName?.charAt(0) || '';
    return (first + last).toUpperCase();
  }
}

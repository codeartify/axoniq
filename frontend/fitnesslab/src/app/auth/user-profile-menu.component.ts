import {Component, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import AuthService from './auth.service';
import {TranslateModule} from '@ngx-translate/core';
import {ProfileImageButton} from './profile-image-button';
import {toSignal} from '@angular/core/rxjs-interop';
import {filter} from 'rxjs';


@Component({
  selector: 'app-user-profile-menu',
  standalone: true,
  imports: [CommonModule, TranslateModule, ProfileImageButton],
  template: `
    @if (userProfile()) {
      @let profile = userProfile();
      <div class="relative">
        <app-profile-image-button [userProfile]="profile" (emitToggleMenu)="toggleMenu()"/>

        <!-- Dropdown Menu -->
        @if (isOpen()) {
          <div
            class="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-xl border border-gray-200 py-2 z-50"
          >
            <!-- User Info -->
            <div class="px-4 py-3 border-b border-gray-200">
              <p class="text-sm font-semibold text-gray-900">
                {{ profile.firstName }} {{ profile.lastName }}
              </p>
              <p class="text-xs text-gray-500 mt-1">{{ profile.email }}</p>
              <div class="flex gap-1 mt-2">
                @for (role of profile.roles; track role) {
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
export class UserProfileMenu {
  private authService = inject(AuthService);
  isOpen = signal(false);
  userProfile = toSignal(this.authService.userProfile$.pipe(filter(v => v !== null && v !== undefined)), {
    initialValue: {
      username: "",
      email: "",
      firstName: "",
      lastName: "",
      roles: [],
    }
  });


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
    const first = this.userProfile()?.firstName?.charAt(0) || '';
    const last = this.userProfile()?.lastName?.charAt(0) || '';
    return (first + last).toUpperCase();
  }
}

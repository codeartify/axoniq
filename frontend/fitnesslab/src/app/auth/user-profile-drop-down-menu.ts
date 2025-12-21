import {Component, input, output} from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';
import {UserProfile} from './auth.service';

@Component({
  selector: 'app-user-profile-drop-down-menu',
  imports: [
    TranslatePipe
  ],
  template: `
    @let profile = userProfile();

    @if (isOpen()) {
      <div
        class="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-xl border border-gray-200 py-2 z-50"
      >
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
  `,
})
export class UserProfileDropDownMenu {
  userProfile = input.required<UserProfile>();
  isOpen = input.required<boolean>();

  onMenuClosed = output<void>();
  onLogout = output<void>();

  closeMenu(): void {
    this.onMenuClosed.emit();
  }

  logout(): void {
    this.onLogout.emit();
  }
}

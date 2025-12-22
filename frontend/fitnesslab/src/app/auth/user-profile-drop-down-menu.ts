import {Component, input, output} from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';
import {UserProfile} from './auth.service';
import {UserProfileTitle} from './user-profile-title';
import {UserProfileEmail} from './user-profile-email';
import {UserProfileRoles} from './user-profile-roles';

@Component({
  selector: 'gym-user-profile-drop-down-menu',
  imports: [
    TranslatePipe,
    UserProfileTitle,
    UserProfileEmail,
    UserProfileRoles
  ],
  template: `
    @let profile = userProfile();

    @if (isOpen()) {
      <div class="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-xl border border-gray-200 py-2 z-50">
        <div class="px-4 py-3 border-b border-gray-200">
          <gym-user-profile-title [userProfile]="profile"/>
          <gym-user-profile-email [email]="profile.email"/>
          <gym-user-profile-roles [roles]="profile.roles"/>
        </div>

        <button class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
                (click)="logout()">
          {{ 'auth.logout' | translate }}
        </button>
      </div>
    }

    @if (isOpen()) {
      <!-- Backdrop -->
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

  menuClosed = output<void>();
  loggedOut = output<void>();

  closeMenu(): void {
    this.menuClosed.emit();
  }

  logout(): void {
    this.loggedOut.emit();
  }
}

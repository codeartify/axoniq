import {Component, input, output} from '@angular/core';
import {UserProfile} from '../auth.service';
import {UserProfileTitle} from './user-profile-title';
import {UserProfileEmail} from './user-profile-email';
import {UserProfileRoles} from './user-profile-roles';
import {LogoutButton} from './logout-button';
import {Backdrop} from './backdrop';

@Component({
  selector: 'gym-user-profile-drop-down-menu',
  imports: [
    UserProfileTitle,
    UserProfileEmail,
    UserProfileRoles,
    LogoutButton,
    Backdrop
  ],
  template: `
    @let profile = userProfile();
    @let title = profile.firstName + ' ' + profile.lastName;

    @if (isOpen()) {
      <div class="absolute right-0 mt-2 w-64 bg-slate-800 rounded-lg shadow-xl border border-slate-700 py-2 z-50">
        <div class="px-4 py-3 border-b border-slate-700">
          <gym-user-profile-title [title]="title"/>
          <gym-user-profile-email [email]="profile.email"/>
          <gym-user-profile-roles [roles]="profile.roles"/>
        </div>

        <gym-logout-button (loggedOut)="logout()"/>
      </div>
    }

    @if (isOpen()) {
      <gym-backdrop (menuClosed)="closeMenu()"/>
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

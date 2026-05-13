import {Component, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import AuthService, {UserProfile} from '../auth.service';
import {TranslateModule} from '@ngx-translate/core';
import {UserProfileImageButton} from './user-profile-image-button';
import {toSignal} from '@angular/core/rxjs-interop';
import {UserProfileDropDownMenu} from './user-profile-drop-down-menu';

@Component({
  selector: 'gym-user-profile-menu',
  standalone: true,
  imports: [CommonModule, TranslateModule, UserProfileImageButton, UserProfileDropDownMenu],
  template: `
    @if (userProfile(); as profile) {
      <div class="relative">
        <gym-user-profile-image-button
          [userProfileButton]="profile"
          (menuToggled)="toggleMenu()"/>

        <gym-user-profile-drop-down-menu
          [userProfile]="profile"
          [isOpen]="isOpen()"
          (menuClosed)="closeMenu()"
          (loggedOut)="logout()"/>
      </div>
    }
  `,
})
export class UserProfileMenu {
  private authService = inject(AuthService);

  userProfile = toSignal(
    this.authService.userProfile$,
    {initialValue: null as UserProfile | null}
  );

  isOpen = signal(false);

  toggleMenu(): void {
    this.isOpen.update((value) => !value);
  }

  closeMenu(): void {
    this.isOpen.set(false);
  }

  logout(): void {
    this.closeMenu();
  }
}

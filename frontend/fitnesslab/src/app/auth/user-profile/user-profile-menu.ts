import {Component, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import AuthService, {UserProfile} from '../auth.service';
import {TranslateModule} from '@ngx-translate/core';
import {UserProfileImageButton} from './user-profile-image-button';
import {toSignal} from '@angular/core/rxjs-interop';
import {filter} from 'rxjs';
import {UserProfileDropDownMenu} from './user-profile-drop-down-menu';


const defaultProfile = {
  username: "",
  email: "",
  firstName: "",
  lastName: "",
  roles: [],
};

const hasUserProfile = (u: UserProfile | null) => u !== null && u !== undefined;

@Component({
  selector: 'gym-user-profile-menu',
  standalone: true,
  imports: [CommonModule, TranslateModule, UserProfileImageButton, UserProfileDropDownMenu],
  template: `
    @if (userProfile()) {
      @let profile = userProfile();

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
    this.authService.userProfile$.pipe(filter(hasUserProfile)),
    {initialValue: defaultProfile}
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

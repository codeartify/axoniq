import {Component, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import AuthService, {UserProfile} from './auth.service';
import {TranslateModule} from '@ngx-translate/core';
import {ProfileImageButton} from './profile-image-button';
import {toSignal} from '@angular/core/rxjs-interop';
import {filter} from 'rxjs';
import {UserProfileDropDownMenu} from './user-profile-drop-down-menu';


let defaultProfile = {
  username: "",
  email: "",
  firstName: "",
  lastName: "",
  roles: [],
};

let hasUserProfile = (u: UserProfile | null) => u !== null && u !== undefined;

@Component({
  selector: 'app-user-profile-menu',
  standalone: true,
  imports: [CommonModule, TranslateModule, ProfileImageButton, UserProfileDropDownMenu],
  template: `
    @if (userProfile()) {
      @let profile = userProfile();

      <div class="relative">
        <app-profile-image-button
          [userProfile]="profile"
          (emitToggleMenu)="toggleMenu()"/>

        <app-user-profile-drop-down-menu
          [userProfile]="profile"
          [isOpen]="isOpen()"
          (onMenuClosed)="closeMenu()"
          (onLogout)="logout()"/>
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
    this.authService.logout();
  }
}

import {Component, computed, input, output} from '@angular/core';
import {UserProfile} from '../auth.service';

@Component({
  selector: 'gym-user-profile-image-button',
  imports: [],
  template: `
    <button
      (click)="toggleMenu()"
      class="flex items-center justify-center w-10 h-10 rounded-full bg-blue-600 text-white font-semibold hover:bg-blue-700 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 cursor-pointer"
    >
      @if (userProfile(); as profile) {
        @if (profile.picture) {
          <img
            [src]="profile.picture"
            [alt]="profile.firstName + ' ' + profile.lastName"
            class="w-10 h-10 rounded-full object-cover"
          />
        }
        @if (!profile.picture) {
          <span>{{ initials() }}</span>
        }
      }
    </button>
  `,
  styles: ``,
})
export class UserProfileImageButton {
  userProfile = input.required<UserProfile>();
  emitToggleMenu = output<void>();

  toggleMenu() {
    this.emitToggleMenu.emit();
  }

  initials = computed(() => {
    const profile = this.userProfile();
    if (!profile) return '';
    const first = profile.firstName?.charAt(0) || '';
    const last = profile.lastName?.charAt(0) || '';
    return (first + last).toUpperCase();
  });
}

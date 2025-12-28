import {Component, computed, input, output} from '@angular/core';

interface UserProfileButton {
  picture?: string;
  firstName?: string;
  lastName?: string;
}

@Component({
  selector: 'gym-user-profile-image-button',
  template: `
    <button
      (click)="toggleMenu()"
      class="flex items-center justify-center w-10 h-10 rounded-full bg-blue-600 text-white font-semibold hover:bg-blue-700 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 cursor-pointer"
    >
      @if (userProfileButton(); as profileButton) {
        @if (profileButton.picture) {
          <img
            [src]="profileButton.picture"
            [alt]="profileButton.firstName + ' ' + profileButton.lastName"
            class="w-10 h-10 rounded-full object-cover"
          />
        }
        @if (!profileButton.picture) {
          <span>{{ initials() }}</span>
        }
      }
    </button>
  `,
})
export class UserProfileImageButton {
  userProfileButton = input.required<UserProfileButton>();
  menuToggled = output<void>();

  toggleMenu() {
    this.menuToggled.emit();
  }

  initials = computed(() => (this.initialFrom(this.userProfileButton().firstName) + this.initialFrom(this.userProfileButton().lastName)).toUpperCase());

  private initialFrom(name?: string) {
    return name?.charAt(0) || '';
  }
}

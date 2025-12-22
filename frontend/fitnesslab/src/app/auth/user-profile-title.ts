import {Component, input} from '@angular/core';
import {UserProfile} from './auth.service';

@Component({
  selector: 'gym-user-profile-title',
  template: `
    @let profile = userProfile();
    <p class="text-sm font-semibold text-gray-900">
      {{ profile.firstName }} {{ profile.lastName }}
    </p>
  `
})
export class UserProfileTitle {
  userProfile = input.required<UserProfile>();
}

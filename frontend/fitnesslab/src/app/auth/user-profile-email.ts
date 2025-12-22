import {Component, input} from '@angular/core';
import {UserProfile} from './auth.service';

@Component({
  selector: 'gym-user-profile-email',
  imports: [],
  template: `<p class="text-xs text-gray-500 mt-1">{{ email() }}</p>`,
  styles: ``,
})
export class UserProfileEmail {
  email = input.required<string>();
}

import {Component, input} from '@angular/core';

@Component({
  selector: 'gym-user-profile-title',
  template: `
    <p class="text-sm font-semibold text-gray-900">
      {{ title() }}
    </p>
  `
})
export class UserProfileTitle {
  title = input.required<string>();
}

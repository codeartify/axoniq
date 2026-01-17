import {Component, input} from '@angular/core';

@Component({
  selector: 'gym-user-profile-title',
  template: `
    <p class="text-sm font-semibold text-slate-50">
      {{ title() }}
    </p>
  `
})
export class UserProfileTitle {
  title = input.required<string>();
}

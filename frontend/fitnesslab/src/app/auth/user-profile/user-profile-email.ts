import {Component, input} from '@angular/core';

@Component({
  selector: 'gym-user-profile-email',
  imports: [],
  template: `<p class="text-xs text-slate-400 mt-1">{{ email() }}</p>`,
  styles: ``,
})
export class UserProfileEmail {
  email = input.required<string>();
}

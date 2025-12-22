import {Component, input} from '@angular/core';

@Component({
  selector: 'gym-user-profile-roles',
  template: `
    <div class="flex flex-col gap-1 mt-2">
      @for (role of roles(); track role) {
        <span
          class="block w-full px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded"
        >
          {{ role }}
        </span>
      }
    </div>
  `,
})
export class UserProfileRoles {
  roles = input.required<string[]>();
}

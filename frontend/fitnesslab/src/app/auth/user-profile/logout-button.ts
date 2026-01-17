import {Component, inject, output} from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';
import AuthService from '../auth.service';

@Component({
  selector: 'gym-logout-button',
  imports: [
    TranslatePipe
  ],
  template: `
    <button
      class="w-full text-left px-4 py-2 text-sm text-red-400 hover:bg-red-50 transition-colors cursor-pointer"
      (click)="logout()">
      {{ 'auth.logout' | translate }}
    </button>
  `,
  styles: ``,
})
export class LogoutButton {
  private authService = inject(AuthService);

  loggedOut = output<void>();

  logout = () => {
    this.authService.logout();
    this.loggedOut.emit();
  }

}

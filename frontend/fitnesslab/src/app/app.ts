import {Component, inject, signal} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {LoadingBar} from './shared/ui-elements/loading-bar';
import {Header} from './header';
import AuthService from './auth/auth.service';

@Component({
  selector: 'gym-root',
  imports: [RouterOutlet, LoadingBar, TranslateModule, Header],
  template: `
    <div class="min-h-screen bg-gray-50">
      <gym-loading-bar/>
      @if (isLoggedIn()) {
        <gym-header [title]="companyName()"/>
      }
      <main>
        <router-outlet/>
      </main>
    </div>
  `
})
export class App {
  isLoggedIn = inject(AuthService).isLoggedIn;
  companyName = signal<string>('Fitness Management System');

}

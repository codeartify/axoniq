import {Component, inject, signal} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {LoadingBar} from './shared/ui-elements/loading-bar';
import {Header} from './header';
import {Footer} from './shared/footer';
import AuthService from './auth/auth.service';

@Component({
  selector: 'gym-root',
  imports: [RouterOutlet, LoadingBar, TranslateModule, Header, Footer],
  template: `
    <div class="min-h-screen bg-gray-50 flex flex-col">
      <gym-loading-bar/>
      @if (isLoggedIn()) {
        <gym-header [title]="companyName()"/>
      }
      <main class="flex-1">
        <router-outlet/>
      </main>
      @if (isLoggedIn()) {
        <gym-footer/>
      }
    </div>
  `
})
export class App {
  isLoggedIn = inject(AuthService).isLoggedIn;
  companyName = signal<string>('Fitness Management System');

}

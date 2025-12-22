import {Component, inject, signal} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {LoadingBar} from './shared/loading-bar';
import {LanguageSwitcher} from './shared/language-switcher';
import {UserProfileMenu} from './auth/user-profile-menu.component';
import AuthService from './auth/auth.service';

@Component({
  selector: 'gym-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LoadingBar, LanguageSwitcher, UserProfileMenu, TranslateModule],
  templateUrl: './app.html',
  standalone: true
})
export class App {
  protected readonly companyName = signal('FitnessLab');

  isLoggedIn =  inject(AuthService).isLoggedIn;
}

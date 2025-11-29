import {Component, inject, signal} from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { LoadingBar } from './shared/loading-bar';
import { LanguageSwitcher } from './shared/language-switcher';
import { UserProfileMenuComponent } from './auth/user-profile-menu.component';
import AuthService from './auth/auth.service';
import {AsyncPipe} from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LoadingBar, LanguageSwitcher, UserProfileMenuComponent, TranslateModule, AsyncPipe],
  templateUrl: './app.html',
  standalone: true
})
export class App {
  protected readonly companyName = signal('FitnessLab');

  isLoggedIn =  inject(AuthService).isLoggedIn;
}

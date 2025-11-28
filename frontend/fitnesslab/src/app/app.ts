import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { LoadingBar } from './shared/loading-bar';
import { LanguageSwitcher } from './shared/language-switcher';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LoadingBar, LanguageSwitcher, TranslateModule],
  templateUrl: './app.html',
  standalone: true
})
export class App {
  protected readonly companyName = signal('FitnessLab');
}

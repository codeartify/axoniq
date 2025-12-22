import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {LoadingBar} from './shared/loading-bar';
import {Header} from './header';

@Component({
  selector: 'gym-root',
  imports: [RouterOutlet, LoadingBar, TranslateModule, Header],
  templateUrl: './app.html',
  standalone: true
})
export class App {
}

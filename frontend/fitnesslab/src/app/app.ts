import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { LoadingBar } from './shared/loading-bar';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LoadingBar],
  templateUrl: './app.html',
  standalone: true
})
export class App {
  protected readonly companyName = signal('FitnessLab');
}

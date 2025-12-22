import {CommonModule} from '@angular/common';
import {LoadingService} from './loading.service';
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';

@Component({
  selector: 'gym-loading-bar',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (loadingService.isLoading()) {
      <div class="fixed top-0 left-0 right-0 z-50">
        <div class="h-1 bg-blue-600 loading-bar"></div>
      </div>
    }
  `,
  styles: [`
    .loading-bar {
      animation: loading 1.5s ease-in-out infinite;
    }

    @keyframes loading {
      0% {
        transform: translateX(-100%);
      }
      50% {
        transform: translateX(0%);
      }
      100% {
        transform: translateX(100%);
      }
    }
  `]
})
export class LoadingBar {

  loadingService = inject(LoadingService)
}

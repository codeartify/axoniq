import { Component, inject, OnInit } from '@angular/core';
import { VersionService } from './version.service';

@Component({
  selector: 'gym-footer',
  standalone: true,
  template: `
    <footer class="bg-gray-800 text-gray-400 py-3 px-4 text-center text-xs sm:text-sm mt-auto">
      @if (versionService.versionInfo(); as version) {
        <div class="flex flex-col sm:flex-row justify-center items-center gap-2 sm:gap-4">
          <span>Version: {{ version.version }}</span>
          <span class="hidden sm:inline">•</span>
          <span>Build: {{ version.buildTime }}</span>
          @if (version.commit !== 'unknown') {
            <span class="hidden sm:inline">•</span>
            <span>Commit: {{ version.commit.substring(0, 7) }}</span>
          }
        </div>
      }
    </footer>
  `
})
export class Footer implements OnInit {
  versionService = inject(VersionService);

  ngOnInit(): void {
    this.versionService.loadVersion();
  }
}

import { Component, inject, OnInit } from '@angular/core';
import { VersionService } from './version.service';

@Component({
  selector: 'gym-footer',
  standalone: true,
  template: `
    <footer class="bg-slate-950 text-slate-400 py-3 px-4 text-center text-xs sm:text-sm mt-auto border-t border-slate-800">
      @if (versionService.versionInfo(); as version) {
        <div class="flex flex-col sm:flex-row justify-center items-center gap-2 sm:gap-4">
          <span>Version: {{ version.version }}</span>
          <span class="hidden sm:inline">•</span>
          <span>Build: {{ version.buildTime }} CET</span>
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

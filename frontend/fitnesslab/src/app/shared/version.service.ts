import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

export interface VersionInfo {
  version: string;
  buildTime: string;
  commit: string;
}

@Injectable({
  providedIn: 'root'
})
export class VersionService {
  private http = inject(HttpClient);
  versionInfo = signal<VersionInfo | null>(null);

  loadVersion(): void {
    this.http.get<VersionInfo>(`${environment.apiUrl}/api/version`).subscribe({
      next: (info) => this.versionInfo.set(info),
      error: (err) => console.error('Failed to load version info', err)
    });
  }
}

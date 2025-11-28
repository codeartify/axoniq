import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-100">
      <div class="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-gray-900 mb-2">FitnessLab</h1>
          <p class="text-gray-600">{{ isLoading ? 'Loading...' : ('login.welcome' | translate) }}</p>
        </div>

        <button
          *ngIf="!isLoading"
          (click)="login()"
          class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-4 rounded-lg transition duration-200 ease-in-out transform hover:scale-105"
        >
          {{ 'login.signIn' | translate }}
        </button>

        <div *ngIf="isLoading" class="w-full text-center">
          <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>

        <div *ngIf="!isLoading" class="mt-6 text-center text-sm text-gray-500">
          <p>{{ 'login.adminCredentials' | translate }}</p>
          <p class="font-mono mt-1">admin / admin123</p>
        </div>
      </div>
    </div>
  `,
})
export class LoginComponent implements OnInit {
  isLoading = true;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('Login component initialized');
    console.log('Initial isLoading state:', this.isLoading);

    // Check current auth state immediately (might already be initialized)
    const checkAndUpdate = () => {
      console.log('Checking auth state...');
      if (this.authService.isAuthenticated()) {
        console.log('User is authenticated, redirecting...');
        this.router.navigate(['/customers']);
      } else {
        console.log('User is not authenticated, showing login button');
        this.isLoading = false;
        console.log('isLoading set to false, triggering change detection');
        this.cdr.detectChanges();
      }
    };

    // Subscribe to initialization state
    this.authService.isInitialized$.subscribe((initialized) => {
      console.log('OAuth initialized:', initialized, 'isLoading:', this.isLoading);
      if (initialized) {
        checkAndUpdate();
      }
    });
  }

  login(): void {
    console.log('Login button clicked');
    this.authService.login();
  }
}

import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import AuthService from './auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'gym-login',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-100">
      <div class="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-gray-900 mb-2">FitnessLab</h1>
          <p class="text-gray-600">{{ 'login.welcome' | translate }}</p>
        </div>

        <form (ngSubmit)="login()" class="space-y-6">
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700 mb-2">
              {{ 'login.username' | translate }}
            </label>
            <input
              id="username"
              type="text"
              [(ngModel)]="username"
              name="username"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              [disabled]="isLoading"
            />
          </div>

          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
              {{ 'login.password' | translate }}
            </label>
            <input
              id="password"
              type="password"
              [(ngModel)]="password"
              name="password"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              [disabled]="isLoading"
            />
          </div>

          @if (errorMessage) {
            <div class="text-red-600 text-sm text-center">
              {{ errorMessage }}
            </div>
          }

          <button
            type="submit"
            [disabled]="isLoading"
            class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-4 rounded-lg transition duration-200 ease-in-out transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            @if (!isLoading) {
              <span>{{ 'login.signIn' | translate }}</span>
            }
            @if (isLoading) {
              <span class="flex items-center justify-center">
                <div class="inline-block animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                {{ 'login.signingIn' | translate }}
              </span>
            }
          </button>
        </form>
      </div>
    </div>
  `,
})
export class LoginComponent implements OnInit {
  username = '';
  password = '';
  isLoading = false;
  errorMessage = '';

  private authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit(): void {
    // If already authenticated, redirect
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/customers']);
    }
  }

  async login(): Promise<void> {
    if (!this.username || !this.password) {
      this.errorMessage = 'Please enter both username and password';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    try {
      await this.authService.loginWithCredentials(this.username, this.password);
      this.router.navigate(['/customers']);
    } catch (error: unknown) {
      console.error(error);
      const err = error as { error?: { error_description?: string }; error_description?: string };
      this.errorMessage = err.error?.error_description || err.error_description || 'Invalid username or password';
      this.isLoading = false;
    }
  }
}

import {Component, inject, input, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import AuthService from './auth.service';
import {CommonModule} from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';
import {filter, take} from 'rxjs';

@Component({
  selector: 'gym-login',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-slate-700">
      <div class="max-w-md w-full bg-slate-800 rounded-lg shadow-lg p-8">
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-slate-50 mb-2">{{ companyName() }}</h1>
          <p class="text-slate-400">{{ 'login.welcome' | translate : {companyName: companyName()} }}</p>
        </div>

        @if (authService.isLoading$ | async) {
          <div class="text-center text-slate-300">
            {{ 'login.signingIn' | translate }}
          </div>
        } @else {
          <div class="space-y-4">
            @if (authService.error$ | async; as error) {
              <div class="text-red-400 text-sm text-center">
                {{ error.message }}
              </div>
            }

            <button
              type="button"
              class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-4 rounded-lg transition duration-200 ease-in-out transform hover:scale-105"
              (click)="signup()"
            >
              Sign Up
            </button>

            <button
              type="button"
              class="w-full border border-slate-500 hover:bg-slate-700 text-slate-50 font-semibold py-3 px-4 rounded-lg transition duration-200 ease-in-out"
              (click)="login()"
            >
              {{ 'login.signIn' | translate }}
            </button>
          </div>
        }
      </div>
    </div>
  `,
})
export class Login implements OnInit {
  companyName = input<string>('Fitness Management System');

  protected authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  ngOnInit(): void {
    this.authService.isAuthenticated$
      .pipe(
        filter((isAuthenticated) => isAuthenticated),
        take(1)
      )
      .subscribe(() => this.router.navigate([this.getReturnUrl()]));
  }

  login(): void {
    this.authService.login(this.getReturnUrl());
  }

  signup(): void {
    this.authService.signup(this.getReturnUrl());
  }

  private getReturnUrl(): string {
    return this.route.snapshot.queryParamMap.get('returnUrl') || '/dashboard';
  }
}

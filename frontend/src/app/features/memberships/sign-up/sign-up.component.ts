import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SignUpState } from './sign-up.state';

@Component({
  selector: 'app-membership-sign-up',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [SignUpState],
  template: `
    <div class="sign-up-container">
      <h2>Membership Sign-Up</h2>

      @if (state.result()) {
        <div class="success-message">
          <h3>Sign-up Successful!</h3>
          <p><strong>Contract ID:</strong> {{ state.result()!.contractId }}</p>
          <p><strong>Booking ID:</strong> {{ state.result()!.bookingId }}</p>
          <p><strong>Invoice ID:</strong> {{ state.result()!.invoiceId }}</p>
          <button (click)="state.reset()">Sign Up Another Member</button>
        </div>
      } @else {
        <form (ngSubmit)="state.submit()">
          <!-- Customer Info -->
          <section class="form-section">
            <h3>Customer Information</h3>

            <div class="form-group">
              <label for="customerId">Customer ID</label>
              <input
                id="customerId"
                type="text"
                [value]="state.customerForm().customerId"
                (input)="updateCustomerId($event)"
                required
              />
            </div>

            <div class="form-group">
              <label for="name">Name</label>
              <input
                id="name"
                type="text"
                [value]="state.customerForm().name"
                (input)="updateName($event)"
                required
              />
            </div>

            <div class="form-group">
              <label for="email">Email</label>
              <input
                id="email"
                type="email"
                [value]="state.customerForm().email"
                (input)="updateEmail($event)"
                required
              />
            </div>
          </section>

          <!-- Membership Selection -->
          <section class="form-section">
            <h3>Select Membership</h3>

            <div class="variants-grid">
              @for (variant of state.membershipVariants(); track variant.id) {
                <div
                  class="variant-card"
                  [class.selected]="state.selectedVariant()?.id === variant.id"
                  (click)="state.selectVariant(variant)"
                >
                  <h4>{{ variant.name }}</h4>
                  <p class="price">CHF {{ variant.price }}</p>
                  <p class="duration">{{ variant.durationMonths }} months</p>
                </div>
              }
            </div>
          </section>

          <!-- Payment Mode -->
          <section class="form-section">
            <h3>Payment Mode</h3>

            <div class="radio-group">
              <label>
                <input
                  type="radio"
                  name="paymentMode"
                  value="INVOICE_EMAIL"
                  [checked]="state.paymentMode() === 'INVOICE_EMAIL'"
                  (change)="state.setPaymentMode('INVOICE_EMAIL')"
                />
                Invoice by Email
              </label>

              <label>
                <input
                  type="radio"
                  name="paymentMode"
                  value="PAY_ON_SITE"
                  [checked]="state.paymentMode() === 'PAY_ON_SITE'"
                  (change)="state.setPaymentMode('PAY_ON_SITE')"
                />
                Pay On-Site
              </label>
            </div>
          </section>

          @if (state.error()) {
            <div class="error-message">{{ state.error() }}</div>
          }

          <button
            type="submit"
            [disabled]="!state.canSubmit()"
            class="submit-button"
          >
            @if (state.isSubmitting()) {
              Submitting...
            } @else {
              Complete Sign-Up
            }
          </button>
        </form>
      }
    </div>
  `,
  styles: [`
    .sign-up-container {
      max-width: 800px;
      margin: 2rem auto;
      padding: 2rem;
    }

    h2 {
      margin-bottom: 2rem;
    }

    .form-section {
      margin-bottom: 2rem;
      padding: 1.5rem;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
    }

    .form-section h3 {
      margin-top: 0;
      margin-bottom: 1rem;
      font-size: 1.2rem;
    }

    .form-group {
      margin-bottom: 1rem;
    }

    .form-group label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
    }

    .form-group input {
      width: 100%;
      padding: 0.5rem;
      border: 1px solid #ccc;
      border-radius: 4px;
      font-size: 1rem;
    }

    .variants-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 1rem;
    }

    .variant-card {
      padding: 1.5rem;
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      text-align: center;
    }

    .variant-card:hover {
      border-color: #007bff;
      box-shadow: 0 2px 8px rgba(0, 123, 255, 0.2);
    }

    .variant-card.selected {
      border-color: #007bff;
      background-color: #e7f3ff;
    }

    .variant-card h4 {
      margin: 0 0 0.5rem 0;
    }

    .variant-card .price {
      font-size: 1.5rem;
      font-weight: bold;
      color: #007bff;
      margin: 0.5rem 0;
    }

    .variant-card .duration {
      color: #666;
      margin: 0;
    }

    .radio-group {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .radio-group label {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      cursor: pointer;
    }

    .submit-button {
      width: 100%;
      padding: 1rem;
      background-color: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      font-size: 1.1rem;
      cursor: pointer;
      transition: background-color 0.2s;
    }

    .submit-button:hover:not(:disabled) {
      background-color: #0056b3;
    }

    .submit-button:disabled {
      background-color: #ccc;
      cursor: not-allowed;
    }

    .error-message {
      padding: 1rem;
      background-color: #fee;
      border: 1px solid #fcc;
      border-radius: 4px;
      color: #c33;
      margin-bottom: 1rem;
    }

    .success-message {
      padding: 2rem;
      background-color: #efe;
      border: 1px solid #cfc;
      border-radius: 8px;
      text-align: center;
    }

    .success-message h3 {
      color: #3c3;
      margin-top: 0;
    }

    .success-message p {
      margin: 0.5rem 0;
    }

    .success-message button {
      margin-top: 1rem;
      padding: 0.75rem 1.5rem;
      background-color: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
  `]
})
export class SignUpComponent implements OnInit {
  readonly state = inject(SignUpState);

  ngOnInit(): void {
    this.state.loadMembershipVariants();
  }

  updateCustomerId(event: Event): void {
    const input = event.target as HTMLInputElement;
    const current = this.state.customerForm();
    this.state.setCustomerForm({ ...current, customerId: input.value });
  }

  updateName(event: Event): void {
    const input = event.target as HTMLInputElement;
    const current = this.state.customerForm();
    this.state.setCustomerForm({ ...current, name: input.value });
  }

  updateEmail(event: Event): void {
    const input = event.target as HTMLInputElement;
    const current = this.state.customerForm();
    this.state.setCustomerForm({ ...current, email: input.value });
  }
}

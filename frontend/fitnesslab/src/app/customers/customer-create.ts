import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Customers } from './customers';

@Component({
  selector: 'app-customer-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslateModule],
  templateUrl: './customer-create.html'
})
export class CustomerCreate {
  private fb = inject(FormBuilder);
  private customerService = inject(Customers);
  private router = inject(Router);

  customerForm: FormGroup;
  errorMessage: string | null = null;
  isSubmitting = false;

  salutations = ['MR', 'MS', 'MRS', 'MX', 'DR'];

  constructor() {
    this.customerForm = this.fb.group({
      salutation: ['', Validators.required],
      firstName: ['', [Validators.required, Validators.minLength(1)]],
      lastName: ['', [Validators.required, Validators.minLength(1)]],
      dateOfBirth: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: [''],
      street: ['', Validators.required],
      houseNumber: ['', Validators.required],
      postalCode: ['', Validators.required],
      city: ['', Validators.required],
      country: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.customerForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      this.errorMessage = null;

      const formValue = this.customerForm.value;
      const request = {
        salutation: formValue.salutation,
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        dateOfBirth: formValue.dateOfBirth,
        email: formValue.email,
        phoneNumber: formValue.phoneNumber || undefined,
        address: {
          street: formValue.street,
          houseNumber: formValue.houseNumber,
          postalCode: formValue.postalCode,
          city: formValue.city,
          country: formValue.country
        }
      };

      this.customerService.registerCustomer(request).subscribe({
        next: (response) => {
          if (response.customerId) {
            this.router.navigate(['/customers', response.customerId]);
          } else if (response.error) {
            this.errorMessage = response.error;
            this.isSubmitting = false;
          }
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Failed to register customer';
          this.isSubmitting = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/']);
  }
}

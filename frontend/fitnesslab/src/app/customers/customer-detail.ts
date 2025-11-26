import {Component, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {Customers, CustomerView} from './customers';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './customer-detail.html',
  styleUrls: ['./customer-detail.css']
})
export class CustomerDetail implements OnInit {
  customer = signal<CustomerView | null>(null);
  errorMessage = signal<string | null>(null);
  isLoading = signal<boolean>(true);
  isEditMode = signal<boolean>(false);
  isSubmitting = signal<boolean>(false);

  customerForm: FormGroup;
  salutations = ['MR', 'MS', 'MRS', 'MX', 'DR'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private customerService: Customers,
    private fb: FormBuilder
  ) {
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

  ngOnInit(): void {
    const customerId = this.route.snapshot.paramMap.get('id');
    if (customerId) {
      this.loadCustomer(customerId);
    } else {
      this.errorMessage.set('No customer ID provided');
      this.isLoading.set(false);
    }
  }

  loadCustomer(customerId: string): void {
    this.customerService.getCustomer(customerId).subscribe({
      next: (customer) => {
        this.customer.set(customer);
        this.populateForm(customer);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(error.status === 404
          ? 'Customer not found'
          : 'Failed to load customer details');
        this.isLoading.set(false);
      }
    });
  }

  populateForm(customer: CustomerView): void {
    this.customerForm.patchValue({
      salutation: customer.salutation,
      firstName: customer.firstName,
      lastName: customer.lastName,
      dateOfBirth: customer.dateOfBirth,
      email: customer.email,
      phoneNumber: customer.phoneNumber || '',
      street: customer.address.street,
      houseNumber: customer.address.houseNumber,
      postalCode: customer.address.postalCode,
      city: customer.address.city,
      country: customer.address.country
    });
  }

  enableEditMode(): void {
    this.isEditMode.set(true);
  }

  cancelEdit(): void {
    this.isEditMode.set(false);
    if (this.customer()) {
      this.populateForm(this.customer()!);
    }
    this.errorMessage.set(null);
  }

  saveCustomer(): void {
    if (this.customerForm.valid && !this.isSubmitting() && this.customer()) {
      this.isSubmitting.set(true);
      this.errorMessage.set(null);

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

      this.customerService.updateCustomer(this.customer()!.customerId, request).subscribe({
        next: () => {
          this.isEditMode.set(false);
          this.isSubmitting.set(false);
          this.loadCustomer(this.customer()!.customerId);
        },
        error: (error) => {
          this.errorMessage.set(error.error?.message || 'Failed to update customer');
          this.isSubmitting.set(false);
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/customers']);
  }

  createNew(): void {
    this.router.navigate(['/customers/new']);
  }
}

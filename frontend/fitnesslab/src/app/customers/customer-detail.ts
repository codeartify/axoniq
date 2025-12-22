import {Component, OnInit, signal, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {Customers, CustomerView} from './customers';
import {Products, ProductView} from '../products/products';
import {Memberships, MembershipSignUpRequest} from '../memberships/memberships';
import {Invoices, InvoiceView} from '../invoices/invoices';
import {MembershipSignUpRequestDto} from '../generated-api/model/models';

@Component({
  selector: 'gym-customer-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslateModule],
  templateUrl: './customer-detail.html'
})
export class CustomerDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private customerService = inject(Customers);
  private productService = inject(Products);
  private membershipService = inject(Memberships);
  private invoiceService = inject(Invoices);
  private fb = inject(FormBuilder);

  customer = signal<CustomerView | null>(null);
  errorMessage = signal<string | null>(null);
  isLoading = signal<boolean>(true);
  isEditMode = signal<boolean>(false);
  isSubmitting = signal<boolean>(false);
  showProductSelection = signal<boolean>(false);
  availableProducts = signal<ProductView[]>([]);
  isLoadingProducts = signal<boolean>(false);
  isAssigningProduct = signal<boolean>(false);
  successMessage = signal<string | null>(null);
  invoices = signal<InvoiceView[]>([]);
  isLoadingInvoices = signal<boolean>(false);

  customerForm: FormGroup;
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
        this.loadInvoices(customerId);
      },
      error: (err) => {
        this.errorMessage.set(err.status === 404
          ? 'Customer not found'
          : 'Failed to load customer details');
        this.isLoading.set(false);
      }
    });
  }

  loadInvoices(customerId: string): void {
    this.isLoadingInvoices.set(true);
    this.invoiceService.getInvoicesByCustomerId(customerId).subscribe({
      next: (invoices) => {
        this.invoices.set(invoices);
        this.isLoadingInvoices.set(false);
      },
      error: (err) => {
        console.error(err);
        this.isLoadingInvoices.set(false);
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
        error: (err) => {
          this.errorMessage.set(err.error?.message || 'Failed to update customer');
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

  openProductSelection(): void {
    this.showProductSelection.set(true);
    this.isLoadingProducts.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.productService.getAllProducts().subscribe({
      next: (products) => {
        this.availableProducts.set(products);
        this.isLoadingProducts.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load products');
        this.isLoadingProducts.set(false);
      }
    });
  }

  closeProductSelection(): void {
    this.showProductSelection.set(false);
    this.availableProducts.set([]);
  }

  selectProduct(product: ProductView): void {
    const customer = this.customer();
    if (!customer || !product.productId) return;

    this.isAssigningProduct.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const signUpRequest: MembershipSignUpRequest = {
        customerId: customer.customerId,
        productVariantId: product.productId,
        paymentMode: MembershipSignUpRequestDto.PaymentModeEnum.InvoiceEmail,
        startDate: new Date().toISOString().split('T')[0]
      };

      this.membershipService.signUp(signUpRequest).subscribe({
        next: (result) => {
          this.isAssigningProduct.set(false);
          this.successMessage.set(`Membership assigned successfully! Contract ID: ${result.contractId}`);
          this.closeProductSelection();
          this.loadInvoices(customer.customerId);
        },
        error: (err) => {
          this.isAssigningProduct.set(false);
          this.errorMessage.set('Failed to assign membership product');
          console.error(err);
        }
      });

  }
}

import { computed, inject, Injectable, signal } from '@angular/core';
import { MembershipsApiService, MembershipSignUpRequest, MembershipSignUpResult, ProductVariantDto } from '../api/memberships-api.service';

export interface CustomerForm {
  customerId: string;
  name: string;
  email: string;
}

@Injectable()
export class SignUpState {
  private readonly api = inject(MembershipsApiService);

  // Signals
  readonly customerForm = signal<CustomerForm>({
    customerId: '',
    name: '',
    email: ''
  });

  readonly membershipVariants = signal<ProductVariantDto[]>([]);
  readonly selectedVariant = signal<ProductVariantDto | null>(null);
  readonly paymentMode = signal<'PAY_ON_SITE' | 'INVOICE_EMAIL'>('INVOICE_EMAIL');
  readonly isSubmitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<MembershipSignUpResult | null>(null);

  // Computed
  readonly canSubmit = computed(() => {
    const form = this.customerForm();
    const variant = this.selectedVariant();
    return form.customerId && form.name && form.email && variant && !this.isSubmitting();
  });

  // Actions
  loadMembershipVariants(): void {
    this.api.getMembershipVariants().subscribe({
      next: (variants) => this.membershipVariants.set(variants),
      error: (err) => this.error.set('Failed to load membership variants: ' + err.message)
    });
  }

  setCustomerForm(form: CustomerForm): void {
    this.customerForm.set(form);
  }

  selectVariant(variant: ProductVariantDto | null): void {
    this.selectedVariant.set(variant);
  }

  setPaymentMode(mode: 'PAY_ON_SITE' | 'INVOICE_EMAIL'): void {
    this.paymentMode.set(mode);
  }

  submit(): void {
    const form = this.customerForm();
    const variant = this.selectedVariant();

    if (!variant) {
      this.error.set('Please select a membership variant');
      return;
    }

    this.isSubmitting.set(true);
    this.error.set(null);

    const request: MembershipSignUpRequest = {
      customerId: form.customerId,
      customerName: form.name,
      customerEmail: form.email,
      productVariantId: variant.id,
      price: variant.price,
      durationMonths: variant.durationMonths,
      paymentMode: this.paymentMode()
    };

    this.api.signUp(request).subscribe({
      next: (result) => {
        this.result.set(result);
        this.isSubmitting.set(false);
      },
      error: (err) => {
        this.error.set('Sign-up failed: ' + err.message);
        this.isSubmitting.set(false);
      }
    });
  }

  reset(): void {
    this.customerForm.set({ customerId: '', name: '', email: '' });
    this.selectedVariant.set(null);
    this.paymentMode.set('INVOICE_EMAIL');
    this.error.set(null);
    this.result.set(null);
  }
}

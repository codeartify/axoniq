import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Products, CreateProductRequest } from './products';

@Component({
  selector: 'gym-product-create',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './product-create.html'
})
export class ProductCreate {
  private productService = inject(Products);
  private router = inject(Router);

  product: CreateProductRequest = {
    slug: '',
    name: '',
    productType: '',
    audience: 'BOTH' as any,
    requiresMembership: false,
    pricingVariant: {
      pricingModel: 'SUBSCRIPTION' as any,
      flatRate: 0,
      billingCycle: undefined,
      duration: undefined,
      freeTrial: undefined
    },
    behavior: {
      canBePaused: false,
      renewalLeadTimeDays: null,
      maxActivePerCustomer: null,
      maxPurchasesPerBuyer: null,
      numberOfSessions: null
    },
    description: null,
    termsAndConditions: null,
    visibility: 'PUBLIC' as any,
    buyable: true,
    buyerCanCancel: true,
    perks: null
  };

  audiences = ['INTERNAL', 'EXTERNAL', 'BOTH'];
  pricingModels = ['SUBSCRIPTION', 'SINGLE_PAYMENT_FOR_DURATION', 'SINGLE_PAYMENT_UNLIMITED'];
  visibilityOptions = ['PUBLIC', 'HIDDEN', 'ARCHIVED'];
  billingIntervals = ['DAY', 'WEEK', 'MONTH', 'YEAR'];
  isSubmitting = false;
  errorMessage: string | null = null;
  perksInput = '';

  onSubmit(): void {
    if (!this.isFormValid()) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    this.productService.createProduct(this.product).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        if (response.productId) {
          this.router.navigate(['/products', response.productId]);
        }
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = 'Failed to create product. Please try again.';
        console.error(error);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/products']);
  }

  isFormValid(): boolean {
    return !!(
      this.product.slug &&
      this.product.name &&
      this.product.productType &&
      this.product.audience &&
      this.product.pricingVariant.flatRate >= 0
    );
  }

  addPerk(): void {
    if (this.perksInput.trim()) {
      if (!this.product.perks) {
        this.product.perks = [];
      }
      this.product.perks.push(this.perksInput.trim());
      this.perksInput = '';
    }
  }

  removePerk(index: number): void {
    if (this.product.perks) {
      this.product.perks.splice(index, 1);
    }
  }

  toggleBillingCycle(): void {
    if (this.product.pricingVariant.billingCycle) {
      this.product.pricingVariant.billingCycle = undefined;
    } else {
      this.product.pricingVariant.billingCycle = { interval: 'MONTH' as any, count: 1 };
    }
  }

  toggleDuration(): void {
    if (this.product.pricingVariant.duration) {
      this.product.pricingVariant.duration = undefined;
    } else {
      this.product.pricingVariant.duration = { interval: 'MONTH' as any, count: 12 };
    }
  }

  toggleFreeTrial(): void {
    if (this.product.pricingVariant.freeTrial) {
      this.product.pricingVariant.freeTrial = undefined;
    } else {
      this.product.pricingVariant.freeTrial = { interval: 'DAY' as any, count: 7 };
    }
  }
}

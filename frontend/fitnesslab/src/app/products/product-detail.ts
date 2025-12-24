import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Products, ProductView, UpdateProductRequest } from './products';

@Component({
  selector: 'gym-product-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './product-detail.html'
})
export class ProductDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private productService = inject(Products);

  product = signal<ProductView | null>(null);
  isLoading = signal<boolean>(true);
  isEditing = signal<boolean>(false);
  isSaving = signal<boolean>(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  editedProduct: UpdateProductRequest | null = null;
  audiences = ['INTERNAL', 'EXTERNAL', 'BOTH'];
  pricingModels = ['SUBSCRIPTION', 'SINGLE_PAYMENT_FOR_DURATION', 'SINGLE_PAYMENT_UNLIMITED'];
  visibilityOptions = ['PUBLIC', 'HIDDEN', 'ARCHIVED'];
  billingIntervals = ['DAY', 'WEEK', 'MONTH', 'YEAR'];
  perksInput = '';

  ngOnInit(): void {
    const productId = this.route.snapshot.paramMap.get('id');
    if (productId) {
      this.loadProduct(productId);
    }
  }

  loadProduct(productId: string): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.productService.getProduct(productId).subscribe({
      next: (product) => {
        this.product.set(product);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load product');
        this.isLoading.set(false);
      }
    });
  }

  startEdit(): void {
    const prod = this.product();
    if (prod && prod.slug && prod.name && prod.productType && prod.audience &&
        prod.requiresMembership !== undefined && prod.pricingVariant && prod.behavior) {
      this.editedProduct = {
        slug: prod.slug,
        name: prod.name,
        productType: prod.productType,
        audience: prod.audience as any,
        requiresMembership: prod.requiresMembership,
        pricingVariant: {
          pricingModel: prod.pricingVariant.pricingModel,
          flatRate: prod.pricingVariant.flatRate,
          billingCycle: prod.pricingVariant.billingCycle || undefined,
          duration: prod.pricingVariant.duration || undefined,
          freeTrial: prod.pricingVariant.freeTrial || undefined
        },
        behavior: {
          canBePaused: prod.behavior.canBePaused,
          renewalLeadTimeDays: prod.behavior.renewalLeadTimeDays || null,
          maxActivePerCustomer: prod.behavior.maxActivePerCustomer || null,
          maxPurchasesPerBuyer: prod.behavior.maxPurchasesPerBuyer || null,
          numberOfSessions: prod.behavior.numberOfSessions || null
        },
        description: prod.description || null,
        termsAndConditions: prod.termsAndConditions || null,
        visibility: prod.visibility || 'PUBLIC',
        buyable: prod.buyable ?? true,
        buyerCanCancel: prod.buyerCanCancel ?? true,
        perks: prod.perks ? [...prod.perks] : null,
        linkedPlatforms: prod.linkedPlatforms ? [...prod.linkedPlatforms] : null
      };
      this.isEditing.set(true);
      this.successMessage.set(null);
    }
  }

  cancelEdit(): void {
    this.editedProduct = null;
    this.isEditing.set(false);
    this.errorMessage.set(null);
  }

  saveProduct(): void {
    const prod = this.product();
    if (!prod || !prod.productId || !this.editedProduct) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.productService.updateProduct(prod.productId, this.editedProduct).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.isEditing.set(false);
        this.successMessage.set('Product updated successfully');
        this.loadProduct(prod.productId!);
      },
      error: (err) => {
        this.isSaving.set(false);
        this.errorMessage.set('Failed to update product');
        console.error(err);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/products']);
  }

  addPerk(): void {
    if (this.perksInput.trim() && this.editedProduct) {
      if (!this.editedProduct.perks) {
        this.editedProduct.perks = [];
      }
      this.editedProduct.perks.push(this.perksInput.trim());
      this.perksInput = '';
    }
  }

  removePerk(index: number): void {
    if (this.editedProduct?.perks) {
      this.editedProduct.perks.splice(index, 1);
    }
  }

  toggleBillingCycle(): void {
    if (this.editedProduct) {
      if (this.editedProduct.pricingVariant.billingCycle) {
        this.editedProduct.pricingVariant.billingCycle = undefined;
      } else {
        this.editedProduct.pricingVariant.billingCycle = { interval: 'MONTH' as any, count: 1 };
      }
    }
  }

  toggleDuration(): void {
    if (this.editedProduct) {
      if (this.editedProduct.pricingVariant.duration) {
        this.editedProduct.pricingVariant.duration = undefined;
      } else {
        this.editedProduct.pricingVariant.duration = { interval: 'MONTH' as any, count: 12 };
      }
    }
  }

  toggleFreeTrial(): void {
    if (this.editedProduct) {
      if (this.editedProduct.pricingVariant.freeTrial) {
        this.editedProduct.pricingVariant.freeTrial = undefined;
      } else {
        this.editedProduct.pricingVariant.freeTrial = { interval: 'DAY' as any, count: 7 };
      }
    }
  }
}

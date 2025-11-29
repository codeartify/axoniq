import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Products, CreateProductRequest } from './products';

@Component({
  selector: 'app-product-create',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './product-create.html'
})
export class ProductCreate {
  private productService = inject(Products);
  private router = inject(Router);

  product: CreateProductRequest = {
    code: '',
    name: '',
    productType: '',
    audience: 'BOTH',
    requiresMembership: false,
    price: 0,
    behavior: {
      isTimeBased: false,
      isSessionBased: false,
      canBePaused: false,
      autoRenew: false,
      contributesToMembershipStatus: false
    }
  };

  audiences = ['INTERNAL', 'EXTERNAL', 'BOTH'];
  isSubmitting = false;
  errorMessage: string | null = null;

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
      this.product.code &&
      this.product.name &&
      this.product.productType &&
      this.product.audience &&
      this.product.price >= 0
    );
  }
}

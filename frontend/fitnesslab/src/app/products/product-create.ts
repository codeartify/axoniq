import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Products, CreateProductRequest, ProductBehaviorConfig } from './products';

@Component({
  selector: 'app-product-create',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-create.html',
  styleUrls: ['./product-create.css']
})
export class ProductCreate {
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

  constructor(
    private productService: Products,
    private router: Router
  ) {}

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
        console.error('Error creating product:', error);
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

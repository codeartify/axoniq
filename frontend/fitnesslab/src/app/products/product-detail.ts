import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Products, ProductView, UpdateProductRequest } from './products';

@Component({
  selector: 'app-product-detail',
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
    if (prod && prod.code && prod.name && prod.productType && prod.audience &&
        prod.requiresMembership !== undefined && prod.price !== undefined && prod.behavior) {
      this.editedProduct = {
        code: prod.code,
        name: prod.name,
        productType: prod.productType,
        audience: prod.audience as any,
        requiresMembership: prod.requiresMembership,
        price: prod.price,
        behavior: {
          canBePaused: prod.behavior.canBePaused,
          renewalLeadTimeDays: prod.behavior.renewalLeadTimeDays,
          maxActivePerCustomer: prod.behavior.maxActivePerCustomer,
          durationInMonths: prod.behavior.durationInMonths,
          numberOfSessions: prod.behavior.numberOfSessions
        }
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
}

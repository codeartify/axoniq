import {Component, computed, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {Products, ProductView} from './products';
import AuthService from '../auth/auth.service';

type SortColumn = 'name' | 'code' | 'productType' | 'price' | 'audience';
type SortDirection = 'asc' | 'desc';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './product-list.html'
})
export class ProductList implements OnInit {
  private productService = inject(Products);
  private router = inject(Router);
  private authService = inject(AuthService);

  allProducts = signal<ProductView[]>([]);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);
  searchTerm = signal<string>('');
  sortColumn = signal<SortColumn>('name');
  sortDirection = signal<SortDirection>('asc');

  canAddProducts = computed(() => this.authService.hasRole('products.write'));

  products = computed(() => {
    let filtered = this.allProducts();

    // Filter by search term
    const term = this.searchTerm().toLowerCase();
    if (term) {
      filtered = filtered.filter(product =>
        product.name.toLowerCase().includes(term) ||
        product.code.toLowerCase().includes(term)
      );
    }

    // Sort by column
    return [...filtered].sort((a, b) => {
      let comparison = 0;

      switch (this.sortColumn()) {
        case 'name':
          comparison = a.name.localeCompare(b.name);
          break;
        case 'code':
          comparison = a.code.localeCompare(b.code);
          break;
        case 'productType':
          comparison = a.productType.localeCompare(b.productType);
          break;
        case 'price':
          comparison = a.price - b.price;
          break;
        case 'audience':
          comparison = a.audience.localeCompare(b.audience);
          break;
      }

      return this.sortDirection() === 'asc' ? comparison : -comparison;
    });
  });

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.productService.getAllProducts().subscribe({
      next: (products) => {
        this.allProducts.set(products);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load products');
        this.isLoading.set(false);
      }
    });
  }

  viewProduct(productId: string): void {
    this.router.navigate(['/products', productId]);
  }

  createProduct(): void {
    this.router.navigate(['/products/new']);
  }

  onSearchChange(term: string): void {
    this.searchTerm.set(term);
  }

  sortBy(column: SortColumn): void {
    if (this.sortColumn() === column) {
      // Toggle direction if same column
      this.sortDirection.set(this.sortDirection() === 'asc' ? 'desc' : 'asc');
    } else {
      // New column, default to ascending
      this.sortColumn.set(column);
      this.sortDirection.set('asc');
    }
  }

  getSortIcon(column: SortColumn): string {
    if (this.sortColumn() !== column) {
      return '⇅';
    }
    return this.sortDirection() === 'asc' ? '↑' : '↓';
  }
}

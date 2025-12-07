import {Component, computed, inject, OnInit, signal, TemplateRef, ViewChild} from '@angular/core';
import {Router} from '@angular/router';
import {Products, ProductView} from './products';
import AuthService from '../auth/auth.service';
import {GenericListComponent, ColumnDefinition, RowAction, CollectionAction} from '../shared/generic-list/generic-list.component';
import {CommonModule} from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';

type SortColumn = 'name' | 'slug' | 'productType' | 'price' | 'audience' | 'visibility';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [GenericListComponent, CommonModule, TranslateModule],
  template: `
    <app-generic-list
      [titleKey]="'product.list.title'"
      [searchPlaceholderKey]="'product.list.searchPlaceholder'"
      [noItemsFoundKey]="'product.list.noProductsFound'"
      [loadingKey]="'product.list.loadingProducts'"
      [createFirstItemKey]="'button.createFirstProduct'"
      [items]="displayedProducts()"
      [columns]="columns"
      [rowActions]="actions"
      [collectionActions]="collectionActions"
      [isLoading]="isLoading()"
      [errorMessage]="errorMessage()"
      [searchTerm]="searchTerm()"
      [sortColumn]="sortColumn()"
      [sortDirection]="sortDirection()"
      [trackByFn]="trackByProductId"
      [onRowClick]="viewProduct.bind(this)"
      (searchTermChange)="onSearchChange($event)"
      (sortChange)="onSortChange($event)"
    />

    <ng-template #requiresMembershipTemplate let-product>
      {{ product.requiresMembership ? ('common.yes' | translate) : ('common.no' | translate) }}
    </ng-template>
  `
})
export class ProductList implements OnInit {
  private productService = inject(Products);
  private router = inject(Router);
  private authService = inject(AuthService);

  @ViewChild('requiresMembershipTemplate', { static: true }) requiresMembershipTemplate!: TemplateRef<any>;

  allProducts = signal<ProductView[]>([]);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);
  searchTerm = signal<string>('');
  sortColumn = signal<SortColumn>('name');
  sortDirection = signal<'asc' | 'desc'>('asc');

  canAddProducts = computed(() => this.authService.hasRole('products.write'));

  columns: ColumnDefinition<ProductView>[] = [
    {
      key: 'slug',
      headerKey: 'product.table.slug',
      sortable: true
    },
    {
      key: 'name',
      headerKey: 'product.table.name',
      sortable: true
    },
    {
      key: 'productType',
      headerKey: 'product.table.type',
      sortable: true
    },
    {
      key: 'price',
      headerKey: 'product.table.price',
      sortable: true,
      getValue: (product) => product.pricingVariant?.flatRate ?? 0
    },
    {
      key: 'visibility',
      headerKey: 'product.table.visibility',
      sortable: true
    },
    {
      key: 'audience',
      headerKey: 'product.table.audience',
      sortable: true
    }
  ];

  actions: RowAction<ProductView>[] = [
    {
      labelKey: 'button.viewDetails',
      onClick: (product) => this.viewProduct(product),
      stopPropagation: true
    }
  ];

  collectionActions: CollectionAction[] = [
    {
      labelKey: 'button.addProduct',
      onClick: () => this.createProduct(),
      show: this.canAddProducts()
    }
  ];

  displayedProducts = computed(() => {
    let filtered = this.allProducts();

    // Filter by search term
    const term = this.searchTerm().toLowerCase();
    if (term) {
      filtered = filtered.filter(product =>
        product.name?.toLowerCase().includes(term) ||
        product.slug?.toLowerCase().includes(term)
      );
    }

    // Sort by column
    return [...filtered].sort((a, b) => {
      let comparison = 0;

      switch (this.sortColumn()) {
        case 'name':
          comparison = (a.name || '').localeCompare(b.name || '');
          break;
        case 'slug':
          comparison = (a.slug || '').localeCompare(b.slug || '');
          break;
        case 'productType':
          comparison = (a.productType || '').localeCompare(b.productType || '');
          break;
        case 'price':
          comparison = (a.pricingVariant?.flatRate || 0) - (b.pricingVariant?.flatRate || 0);
          break;
        case 'visibility':
          comparison = (a.visibility || '').localeCompare(b.visibility || '');
          break;
        case 'audience':
          comparison = (a.audience || '').localeCompare(b.audience || '');
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

  viewProduct(product: ProductView): void {
    this.router.navigate(['/products', product.productId]);
  }

  createProduct(): void {
    this.router.navigate(['/products/new']);
  }

  onSearchChange(term: string): void {
    this.searchTerm.set(term);
  }

  onSortChange(event: { column: string, direction: 'asc' | 'desc' }): void {
    this.sortColumn.set(event.column as SortColumn);
    this.sortDirection.set(event.direction);
  }

  trackByProductId(index: number, product: ProductView): string {
    return product.productId || `index-${index}`;
  }
}

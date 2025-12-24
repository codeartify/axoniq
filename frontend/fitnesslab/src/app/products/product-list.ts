import {Component, computed, effect, inject, input, signal, TemplateRef, ViewChild} from '@angular/core';
import {Router} from '@angular/router';
import {Products, ProductView} from './products';
import AuthService from '../auth/auth.service';
import {CollectionAction, ColumnDefinition, GenericList, RowAction} from '../shared/generic-list/generic-list';
import {CommonModule} from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';
import {switchMap, take} from 'rxjs';

type SortColumn = 'name' | 'slug' | 'productType' | 'price' | 'audience' | 'visibility';

@Component({
  selector: 'gym-product-list',
  standalone: true,
  imports: [GenericList, CommonModule, TranslateModule],
  template: `
    <gym-generic-list
      [titleKey]="'product.list.title'"
      [searchPlaceholderKey]="'product.list.searchPlaceholder'"
      [noItemsFoundKey]="'product.list.noProductsFound'"
      [loadingKey]="'product.list.loadingProducts'"
      [createFirstItemKey]="'button.createFirstProduct'"
      [items]="displayedProducts()"
      [columns]="columns"
      [rowActions]="actions"
      [collectionActions]="collectionActions"
      [isLoading]="false"
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
export class ProductList {
  private router = inject(Router);
  private authService = inject(AuthService);
  private productService = inject(Products);


  @ViewChild('requiresMembershipTemplate', { static: true }) requiresMembershipTemplate!: TemplateRef<any>;

  productsFromResolve = input.required<ProductView[]>();
  allProducts = signal<ProductView[]>([]);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);
  searchTerm = signal<string>('');
  sortColumn = signal<SortColumn>('name');
  sortDirection = signal<'asc' | 'desc'>('asc');

  canAddProducts = computed(() => this.authService.hasRole('products.write'));

  constructor() {
    effect(() => {
      this.allProducts.set(this.productsFromResolve());
    });
  }

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
      key: 'wix',
      headerKey: 'product.table.wix',
      sortable: false,
      getValue: (product) => this.getWixSyncStatus(product)
    }
  ];

  actions: RowAction<ProductView>[] = [
    {
      labelKey: 'button.viewDetails',
      onClick: (product) => this.viewProduct(product),
      isDisabled: (product) => this.hasIncomingWixChanges(product),
      stopPropagation: true
    },
    {
      labelKey: 'products.checkForWixUpdates',
      onClick: (product) => this.checkForWixUpdates(product.productId!!),
      isDisabled: (product) => !this.isLinkedWithWix(product),
      stopPropagation: true,
    },
    {
      labelKey: 'products.downloadFromWix',
      onClick: (product) => this.downloadProductFromWix(product.productId!!),
      isDisabled: (product) => !this.hasIncomingWixChanges(product),
      stopPropagation: true,
    },
    {
      labelKey: 'products.uploadToWix',
      onClick: (product) => this.uploadToWix(product.productId!!),
      isDisabled: (product) => !this.hasLocalChanges(product) || this.hasIncomingWixChanges(product),
      stopPropagation: true,
    }
  ];

  private isLinkedWithWix(product: ProductView) {
    return !!(product.linkedPlatforms && product.linkedPlatforms.find(platform => platform.platformName.toLowerCase() === 'wix'));
  }

  private getWixPlatform(product: ProductView) {
    return product.linkedPlatforms?.find(platform => platform.platformName.toLowerCase() === 'wix');
  }

  private hasLocalChanges(product: ProductView): boolean {
    const wixPlatform = this.getWixPlatform(product);
    return wixPlatform?.hasLocalChanges === true;
  }

  private hasIncomingWixChanges(product: ProductView): boolean {
    const wixPlatform = this.getWixPlatform(product);
    return wixPlatform?.hasIncomingChanges === true;
  }

  private getWixSyncStatus(product: ProductView): string {
    const wixPlatform = this.getWixPlatform(product);

    if (!wixPlatform) {
      return '✖'; // Not linked
    }

    const hasLocal = wixPlatform.hasLocalChanges === true;
    const hasIncoming = wixPlatform.hasIncomingChanges === true;

    if (hasIncoming && hasLocal) {
      return '🔄 🔴⬇️ 🟢⬆️'; // Synced + red download + green upload
    } else if (hasIncoming) {
      return '🔄 🔴⬇️'; // Synced + red download (incoming changes)
    } else if (hasLocal) {
      return '🔄 🟢⬆️'; // Synced + green upload (local changes)
    } else {
      return '🔄'; // Synced
    }
  }

  collectionActions: CollectionAction[] = [
    {
      labelKey: 'button.addProduct',
      onClick: () => this.createProduct(),
      show: this.canAddProducts()
    },
    {
      labelKey: 'button.checkForWixUpdatesForAll',
      onClick: () => this.checkForWixUpdatesForAll(),
      show: this.canAddProducts()
    },
    {
      labelKey: 'button.downloadFromWix',
      onClick: () => this.downloadFromWix(),
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

  private uploadToWix(productId: string) {
    this.productService.uploadToWix(productId)
      .pipe(
        switchMap(() => this.productService.getAllProducts()),
        take(1))
      .subscribe((products)=>this.allProducts.set(products));
  }

  private checkForWixUpdates(productId: string) {
    this.productService.checkForWixUpdates(productId)
      .pipe(
        switchMap(() => this.productService.getAllProducts()),
        take(1))
      .subscribe((products)=>this.allProducts.set(products));
  }

  private downloadProductFromWix(productId: string) {
    this.productService.downloadFromWixForProduct(productId)
      .pipe(
        switchMap(() => this.productService.getAllProducts()),
        take(1))
      .subscribe((products)=>this.allProducts.set(products));
  }

  private checkForWixUpdatesForAll() {
    this.productService.checkForWixUpdatesForAll()
      .pipe(
        switchMap(() => this.productService.getAllProducts()),
        take(1))
      .subscribe((products)=>this.allProducts.set(products));
  }

  private downloadFromWix() {
    this.productService.downloadFromWix()
      .pipe(
        switchMap(() => this.productService.getAllProducts()),
        take(1))
      .subscribe((products)=>this.allProducts.set(products));

  }
}

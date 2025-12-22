import {Component, computed, inject, input, signal} from '@angular/core';
import {Router} from '@angular/router';
import {CustomerView} from './customers';
import {CollectionAction, ColumnDefinition, GenericList, RowAction} from '../shared/generic-list/generic-list';

type SortColumn = 'name' | 'email' | 'phone' | 'city' | 'dateOfBirth';

@Component({
  selector: 'gym-customer-list',
  standalone: true,
  imports: [GenericList],
  template: `
    <gym-generic-list
      [titleKey]="'customer.list.title'"
      [searchPlaceholderKey]="'customer.list.searchPlaceholder'"
      [noItemsFoundKey]="'customer.list.noCustomersFound'"
      [loadingKey]="'customer.list.loadingCustomers'"
      [createFirstItemKey]="'button.createFirstCustomer'"
      [items]="displayedCustomers()"
      [columns]="columns"
      [rowActions]="actions"
      [collectionActions]="collectionActions"
      [isLoading]="false"
      [errorMessage]="errorMessage()"
      [searchTerm]="searchTerm()"
      [sortColumn]="sortColumn()"
      [sortDirection]="sortDirection()"
      [trackByFn]="trackByCustomerId"
      [onRowClick]="viewCustomer.bind(this)"
      (searchTermChange)="onSearchChange($event)"
      (sortChange)="onSortChange($event)"
    />
  `
})
export class CustomerList {
  allCustomers = input.required<CustomerView[]>();

  private router = inject(Router);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);
  searchTerm = signal<string>('');
  sortColumn = signal<SortColumn>('name');
  sortDirection = signal<'asc' | 'desc'>('asc');

  columns: ColumnDefinition<CustomerView>[] = [
    {
      key: 'name',
      headerKey: 'customer.table.name',
      sortable: true,
      getValue: (customer) => this.getFullName(customer)
    },
    {
      key: 'email',
      headerKey: 'customer.table.email',
      sortable: true
    },
    {
      key: 'phoneNumber',
      headerKey: 'customer.table.phone',
      sortable: true,
      getValue: (customer) => customer.phoneNumber || 'N/A'
    },
    {
      key: 'address.city',
      headerKey: 'customer.table.city',
      sortable: true,
      getValue: (customer) => customer.address.city
    },
    {
      key: 'dateOfBirth',
      headerKey: 'customer.table.dateOfBirth',
      sortable: true
    }
  ];

  actions: RowAction<CustomerView>[] = [
    {
      labelKey: 'button.viewDetails',
      onClick: (customer) => this.viewCustomer(customer),
      stopPropagation: true
    }
  ];

  collectionActions: CollectionAction[] = [
    {
      labelKey: 'button.addCustomer',
      onClick: () => this.createCustomer()
    }
  ];

  displayedCustomers = computed(() => {
    let filtered = this.allCustomers();

    // Filter by search term
    const term = this.searchTerm().toLowerCase();
    if (term) {
      filtered = filtered.filter(customer =>
        this.getFullName(customer).toLowerCase().includes(term)
      );
    }

    // Sort by column
    return [...filtered].sort((a, b) => {
      let comparison = 0;

      switch (this.sortColumn()) {
        case 'name':
          comparison = this.getFullName(a).localeCompare(this.getFullName(b));
          break;
        case 'email':
          comparison = a.email.localeCompare(b.email);
          break;
        case 'phone':
          comparison = (a.phoneNumber || '').localeCompare(b.phoneNumber || '');
          break;
        case 'city':
          comparison = a.address.city.localeCompare(b.address.city);
          break;
        case 'dateOfBirth':
          comparison = a.dateOfBirth.localeCompare(b.dateOfBirth);
          break;
      }

      return this.sortDirection() === 'asc' ? comparison : -comparison;
    });
  });


  viewCustomer(customer: CustomerView): void {
    this.router.navigate(['/customers', customer.customerId]);
  }

  createCustomer(): void {
    this.router.navigate(['/customers/new']);
  }

  getFullName(customer: CustomerView): string {
    return `${customer.salutation} ${customer.firstName} ${customer.lastName}`;
  }

  onSearchChange(term: string): void {
    this.searchTerm.set(term);
  }

  onSortChange(event: { column: string, direction: 'asc' | 'desc' }): void {
    this.sortColumn.set(event.column as SortColumn);
    this.sortDirection.set(event.direction);
  }

  trackByCustomerId(index: number, customer: CustomerView): string {
    return customer.customerId;
  }
}

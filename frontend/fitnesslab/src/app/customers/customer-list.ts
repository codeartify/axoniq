import {Component, computed, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {Customers, CustomerView} from './customers';

type SortColumn = 'name' | 'email' | 'phone' | 'city' | 'dateOfBirth';
type SortDirection = 'asc' | 'desc';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './customer-list.html'
})
export class CustomerList implements OnInit {
  allCustomers = signal<CustomerView[]>([]);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);
  searchTerm = signal<string>('');
  sortColumn = signal<SortColumn>('name');
  sortDirection = signal<SortDirection>('asc');

  customers = computed(() => {
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

  constructor(
    private customerService: Customers,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.customerService.getAllCustomers().subscribe({
      next: (customers) => {
        this.allCustomers.set(customers);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set('Failed to load customers');
        this.isLoading.set(false);
      }
    });
  }

  viewCustomer(customerId: string): void {
    this.router.navigate(['/customers', customerId]);
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

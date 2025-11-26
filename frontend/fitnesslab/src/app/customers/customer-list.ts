import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Customers, CustomerView } from './customers';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-list.html',
  styleUrls: ['./customer-list.css']
})
export class CustomerList implements OnInit {
  customers = signal<CustomerView[]>([]);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);

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
        this.customers.set(customers);
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
}

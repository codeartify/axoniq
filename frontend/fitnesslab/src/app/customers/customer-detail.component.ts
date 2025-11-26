import {Component, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {CustomerService, CustomerView} from './customer.service';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-detail.component.html',
  styleUrls: ['./customer-detail.component.css']
})
export class CustomerDetailComponent implements OnInit {
  customer: CustomerView | null = null;
  errorMessage: string | null = null;
  isLoading = signal(true);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private customerService: CustomerService
  ) {
  }

  ngOnInit(): void {
    const customerId = this.route.snapshot.paramMap.get('id');
    if (customerId) {
      this.loadCustomer(customerId);
    } else {
      this.errorMessage = 'No customer ID provided';
      this.isLoading.set(false);
    }
  }

  loadCustomer(customerId: string): void {
    this.customerService.getCustomer(customerId).subscribe({
      next: (customer) => {
        this.customer = customer;
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage = error.status === 404
          ? 'Customer not found'
          : 'Failed to load customer details';
        this.isLoading.set(false);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/customers']);
  }

  createNew(): void {
    this.router.navigate(['/customers/new']);
  }
}

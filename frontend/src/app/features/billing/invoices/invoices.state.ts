import { computed, inject, Injectable, signal } from '@angular/core';
import { BillingApiService, InvoiceDto } from '../api/billing-api.service';

export type InvoiceStatusFilter = 'ALL' | 'OPEN' | 'OVERDUE' | 'PAID';

@Injectable()
export class InvoicesState {
  private readonly api = inject(BillingApiService);

  // Signals
  readonly invoices = signal<InvoiceDto[]>([]);
  readonly selectedInvoice = signal<InvoiceDto | null>(null);
  readonly filterStatus = signal<InvoiceStatusFilter>('OPEN');
  readonly isLoading = signal(false);
  readonly payInProgress = signal(false);
  readonly error = signal<string | null>(null);

  // Computed
  readonly filteredInvoices = computed(() => {
    const filter = this.filterStatus();
    const allInvoices = this.invoices();

    if (filter === 'ALL') {
      return allInvoices;
    }

    return allInvoices.filter(inv => inv.status === filter);
  });

  // Actions
  loadInvoices(status?: string): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.api.getInvoices(status).subscribe({
      next: (invoices) => {
        this.invoices.set(invoices);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to load invoices: ' + err.message);
        this.isLoading.set(false);
      }
    });
  }

  setFilter(status: InvoiceStatusFilter): void {
    this.filterStatus.set(status);
    if (status === 'ALL') {
      this.loadInvoices();
    } else {
      this.loadInvoices(status);
    }
  }

  selectInvoice(invoice: InvoiceDto | null): void {
    this.selectedInvoice.set(invoice);
  }

  markAsPaid(invoiceId: string): void {
    this.payInProgress.set(true);
    this.error.set(null);

    this.api.markAsPaid(invoiceId).subscribe({
      next: () => {
        this.payInProgress.set(false);
        // Reload invoices to get updated state
        this.loadInvoices(this.filterStatus() === 'ALL' ? undefined : this.filterStatus());
        // Clear selection
        this.selectedInvoice.set(null);
      },
      error: (err) => {
        this.error.set('Failed to mark invoice as paid: ' + err.message);
        this.payInProgress.set(false);
      }
    });
  }
}

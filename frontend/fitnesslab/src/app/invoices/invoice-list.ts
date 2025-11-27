import {Component, computed, effect, signal} from '@angular/core';
import {InvoiceView, Invoices} from './invoices';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';

type SortColumn = 'invoiceId' | 'customerName' | 'amount' | 'dueDate' | 'status';

@Component({
  selector: 'app-invoice-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './invoice-list.html'
})
export class InvoiceList {
  invoices = signal<InvoiceView[]>([]);
  searchTerm = signal('');
  sortColumn = signal<SortColumn>('dueDate');
  sortDirection = signal<'asc' | 'desc'>('desc');
  showCancelModal = signal(false);
  selectedInvoiceId = signal<string | null>(null);
  cancelReason = signal('');

  filteredAndSortedInvoices = computed(() => {
    let result = this.invoices();

    // Filter
    const search = this.searchTerm().toLowerCase();
    if (search) {
      result = result.filter(invoice =>
        invoice.invoiceId.toLowerCase().includes(search) ||
        invoice.customerId.toLowerCase().includes(search) ||
        invoice.customerName.toLowerCase().includes(search) ||
        invoice.bookingId.toLowerCase().includes(search) ||
        invoice.amount.toString().includes(search) ||
        invoice.status.toLowerCase().includes(search)
      );
    }

    // Sort
    const column = this.sortColumn();
    const direction = this.sortDirection();
    result = [...result].sort((a, b) => {
      let aVal: any = a[column];
      let bVal: any = b[column];

      if (column === 'amount') {
        aVal = Number(aVal);
        bVal = Number(bVal);
      } else if (column === 'dueDate') {
        aVal = new Date(aVal).getTime();
        bVal = new Date(bVal).getTime();
      }

      if (aVal < bVal) return direction === 'asc' ? -1 : 1;
      if (aVal > bVal) return direction === 'asc' ? 1 : -1;
      return 0;
    });

    return result;
  });

  constructor(
    private invoiceService: Invoices,
    private router: Router
  ) {
    effect(() => {
      this.loadInvoices();
    }, { allowSignalWrites: true });
  }

  loadInvoices(): void {
    this.invoiceService.getAllInvoices().subscribe({
      next: (invoices) => this.invoices.set(invoices),
      error: (err) => console.error('Failed to load invoices', err)
    });
  }

  sortBy(column: SortColumn): void {
    if (this.sortColumn() === column) {
      this.sortDirection.set(this.sortDirection() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortColumn.set(column);
      this.sortDirection.set('asc');
    }
  }

  getSortIcon(column: SortColumn): string {
    if (this.sortColumn() !== column) return '↕';
    return this.sortDirection() === 'asc' ? '↑' : '↓';
  }

  markAsPaid(invoiceId: string): void {
    this.invoiceService.markInvoiceAsPaid(invoiceId).subscribe({
      next: () => this.loadInvoices(),
      error: (err) => alert(`Failed to mark invoice as paid: ${err.error?.message || err.message}`)
    });
  }

  markAsOverdue(invoiceId: string): void {
    this.invoiceService.markInvoiceAsOverdue(invoiceId).subscribe({
      next: () => this.loadInvoices(),
      error: (err) => alert(`Failed to mark invoice as overdue: ${err.error?.message || err.message}`)
    });
  }

  openCancelModal(invoiceId: string): void {
    this.selectedInvoiceId.set(invoiceId);
    this.cancelReason.set('');
    this.showCancelModal.set(true);
  }

  closeCancelModal(): void {
    this.showCancelModal.set(false);
    this.selectedInvoiceId.set(null);
    this.cancelReason.set('');
  }

  confirmCancel(): void {
    const invoiceId = this.selectedInvoiceId();
    const reason = this.cancelReason();

    if (!invoiceId || !reason.trim()) {
      alert('Please provide a cancellation reason');
      return;
    }

    this.invoiceService.cancelInvoice(invoiceId, reason).subscribe({
      next: () => {
        this.loadInvoices();
        this.closeCancelModal();
      },
      error: (err) => alert(`Failed to cancel invoice: ${err.error?.message || err.message}`)
    });
  }

  viewCustomer(customerId: string): void {
    this.router.navigate(['/customers', customerId]);
  }

}

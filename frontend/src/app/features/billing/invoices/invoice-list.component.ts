import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InvoicesState, InvoiceStatusFilter } from './invoices.state';
import { InvoiceDto } from '../api/billing-api.service';

@Component({
  selector: 'app-invoice-list',
  standalone: true,
  imports: [CommonModule],
  providers: [InvoicesState],
  template: `
    <div class="invoice-list-container">
      <h2>Invoices</h2>

      <!-- Filter tabs -->
      <div class="filter-tabs">
        <button
          *ngFor="let status of filterOptions"
          [class.active]="state.filterStatus() === status"
          (click)="state.setFilter(status)"
        >
          {{ status }}
        </button>
      </div>

      @if (state.error()) {
        <div class="error-message">{{ state.error() }}</div>
      }

      @if (state.isLoading()) {
        <div class="loading">Loading invoices...</div>
      } @else {
        <!-- Invoice list -->
        <div class="invoice-table">
          <div class="table-header">
            <div>Invoice ID</div>
            <div>Customer ID</div>
            <div>Amount</div>
            <div>Due Date</div>
            <div>Status</div>
            <div>Actions</div>
          </div>

          @for (invoice of state.filteredInvoices(); track invoice.invoiceId) {
            <div class="table-row" [class.selected]="state.selectedInvoice()?.invoiceId === invoice.invoiceId">
              <div>{{ invoice.invoiceId.substring(0, 8) }}...</div>
              <div>{{ invoice.customerId.substring(0, 8) }}...</div>
              <div>CHF {{ invoice.amount }}</div>
              <div>{{ invoice.dueDate }}</div>
              <div>
                <span [class]="'status-badge status-' + invoice.status.toLowerCase()">
                  {{ invoice.status }}
                </span>
              </div>
              <div class="actions">
                <button (click)="selectInvoice(invoice)" class="btn-view">View</button>
                @if (invoice.status === 'OPEN' || invoice.status === 'OVERDUE') {
                  <button
                    (click)="markAsPaid(invoice.invoiceId)"
                    [disabled]="state.payInProgress()"
                    class="btn-pay"
                  >
                    Pay
                  </button>
                }
              </div>
            </div>
          } @empty {
            <div class="empty-state">No invoices found</div>
          }
        </div>
      }

      <!-- Invoice detail -->
      @if (state.selectedInvoice(); as invoice) {
        <div class="invoice-detail">
          <h3>Invoice Details</h3>
          <button class="close-btn" (click)="state.selectInvoice(null)">×</button>

          <div class="detail-grid">
            <div class="detail-item">
              <label>Invoice ID:</label>
              <span>{{ invoice.invoiceId }}</span>
            </div>
            <div class="detail-item">
              <label>Customer ID:</label>
              <span>{{ invoice.customerId }}</span>
            </div>
            <div class="detail-item">
              <label>Booking ID:</label>
              <span>{{ invoice.bookingId }}</span>
            </div>
            <div class="detail-item">
              <label>Amount:</label>
              <span>CHF {{ invoice.amount }}</span>
            </div>
            <div class="detail-item">
              <label>Due Date:</label>
              <span>{{ invoice.dueDate }}</span>
            </div>
            <div class="detail-item">
              <label>Status:</label>
              <span [class]="'status-badge status-' + invoice.status.toLowerCase()">
                {{ invoice.status }}
              </span>
            </div>
            @if (invoice.paidAt) {
              <div class="detail-item">
                <label>Paid At:</label>
                <span>{{ invoice.paidAt }}</span>
              </div>
            }
            @if (invoice.isInstallment) {
              <div class="detail-item">
                <label>Installment:</label>
                <span>{{ invoice.installmentNumber }} of 3</span>
              </div>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .invoice-list-container {
      max-width: 1200px;
      margin: 2rem auto;
      padding: 2rem;
    }

    h2 {
      margin-bottom: 1.5rem;
    }

    .filter-tabs {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 1.5rem;
    }

    .filter-tabs button {
      padding: 0.5rem 1rem;
      border: 1px solid #ddd;
      background: white;
      cursor: pointer;
      border-radius: 4px;
      transition: all 0.2s;
    }

    .filter-tabs button.active {
      background: #007bff;
      color: white;
      border-color: #007bff;
    }

    .invoice-table {
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      overflow: hidden;
    }

    .table-header, .table-row {
      display: grid;
      grid-template-columns: 1.5fr 1.5fr 1fr 1fr 1fr 1.5fr;
      gap: 1rem;
      padding: 1rem;
      align-items: center;
    }

    .table-header {
      background: #f5f5f5;
      font-weight: 600;
      border-bottom: 2px solid #e0e0e0;
    }

    .table-row {
      border-bottom: 1px solid #f0f0f0;
      transition: background-color 0.2s;
    }

    .table-row:hover {
      background: #f9f9f9;
    }

    .table-row.selected {
      background: #e7f3ff;
    }

    .status-badge {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 0.875rem;
      font-weight: 500;
    }

    .status-open {
      background: #fff3cd;
      color: #856404;
    }

    .status-paid {
      background: #d4edda;
      color: #155724;
    }

    .status-overdue {
      background: #f8d7da;
      color: #721c24;
    }

    .status-cancelled {
      background: #e0e0e0;
      color: #666;
    }

    .actions {
      display: flex;
      gap: 0.5rem;
    }

    .btn-view, .btn-pay {
      padding: 0.4rem 0.8rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.875rem;
    }

    .btn-view {
      background: #6c757d;
      color: white;
    }

    .btn-pay {
      background: #28a745;
      color: white;
    }

    .btn-pay:disabled {
      background: #ccc;
      cursor: not-allowed;
    }

    .empty-state {
      padding: 3rem;
      text-align: center;
      color: #999;
    }

    .loading, .error-message {
      padding: 1rem;
      margin-bottom: 1rem;
      border-radius: 4px;
    }

    .loading {
      background: #e7f3ff;
      color: #004085;
    }

    .error-message {
      background: #fee;
      border: 1px solid #fcc;
      color: #c33;
    }

    .invoice-detail {
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: white;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      padding: 2rem;
      box-shadow: 0 4px 16px rgba(0,0,0,0.1);
      max-width: 600px;
      width: 90%;
    }

    .invoice-detail h3 {
      margin-top: 0;
    }

    .close-btn {
      position: absolute;
      top: 1rem;
      right: 1rem;
      background: none;
      border: none;
      font-size: 2rem;
      cursor: pointer;
      color: #999;
      line-height: 1;
    }

    .detail-grid {
      display: grid;
      gap: 1rem;
    }

    .detail-item {
      display: grid;
      grid-template-columns: 150px 1fr;
      gap: 1rem;
    }

    .detail-item label {
      font-weight: 600;
    }
  `]
})
export class InvoiceListComponent implements OnInit {
  readonly state = inject(InvoicesState);
  readonly filterOptions: InvoiceStatusFilter[] = ['ALL', 'OPEN', 'OVERDUE', 'PAID'];

  ngOnInit(): void {
    this.state.loadInvoices('OPEN');
  }

  selectInvoice(invoice: InvoiceDto): void {
    this.state.selectInvoice(invoice);
  }

  markAsPaid(invoiceId: string): void {
    if (confirm('Mark this invoice as paid?')) {
      this.state.markAsPaid(invoiceId);
    }
  }
}

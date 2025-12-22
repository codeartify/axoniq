import {AfterViewInit, Component, computed, effect, inject, signal, TemplateRef, ViewChild} from '@angular/core';
import {Invoices, InvoiceView} from './invoices';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {ColumnDefinition, GenericList, RowAction} from '../shared/generic-list/generic-list';

type SortColumn = 'invoiceId' | 'customerName' | 'amount' | 'dueDate' | 'status';

@Component({
  selector: 'gym-invoice-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, GenericList],
  template: `
    <gym-generic-list
      [titleKey]="'invoice.list.title'"
      [searchPlaceholderKey]="'invoice.list.searchPlaceholder'"
      [noItemsFoundKey]="'invoice.list.noInvoicesFound'"
      [items]="filteredAndSortedInvoices()"
      [columns]="columns"
      [rowActions]="rowActions"
      [searchTerm]="searchTerm()"
      [sortColumn]="sortColumn()"
      [sortDirection]="sortDirection()"
      [trackByFn]="trackByInvoiceId"
      (searchTermChange)="searchTerm.set($event)"
      (sortChange)="onSortChange($event)"
    />

    <!-- Cancel Modal -->
    @if (showCancelModal()) {
      <div class="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50"
           (click)="closeCancelModal()"
           (keydown.escape)="closeCancelModal()" tabindex="0" role="dialog" aria-modal="true">
        <div class="bg-white p-6 rounded-lg max-w-lg w-11/12 shadow-xl" (click)="$event.stopPropagation()"
             (keydown)="$event.stopPropagation()" tabindex="-1">
          <h2
            class="mt-0 mb-4 text-xl font-semibold text-gray-800">{{ 'invoice.list.cancelModal.title' | translate }}</h2>
          <p class="mb-4">{{ 'invoice.list.cancelModal.reasonPrompt' | translate }}</p>
          <textarea
            [(ngModel)]="cancelReason"
            class="w-full px-3 py-2 border border-gray-300 rounded text-sm resize-y mb-4 focus:outline-none focus:ring-2 focus:ring-blue-500"
            [placeholder]="'invoice.list.cancelModal.reasonPlaceholder' | translate"
            rows="4"
          ></textarea>
          <div class="flex justify-end gap-2">
            <button (click)="closeCancelModal()"
                    class="px-3 py-1.5 bg-gray-500 text-white rounded border-none cursor-pointer text-sm hover:bg-gray-600 transition-colors">
              {{ 'common.close' | translate }}
            </button>
            <button (click)="confirmCancel()"
                    class="px-3 py-1.5 bg-red-500 text-white rounded border-none cursor-pointer text-sm hover:bg-red-600 transition-colors">
              {{ 'button.confirmCancel' | translate }}
            </button>
          </div>
        </div>
      </div>
    }

    <!-- Templates for custom cell rendering -->
    <ng-template #invoiceIdTemplate let-invoice>
      {{ invoice.invoiceId.substring(0, 8) }}...
    </ng-template>

    <ng-template #customerNameTemplate let-invoice>
      <a (click)="viewCustomer(invoice.customerId); $event.stopPropagation()"
         (keydown.enter)="viewCustomer(invoice.customerId)"
         tabindex="0" role="button" class="text-blue-600 cursor-pointer underline hover:text-blue-800">
        {{ invoice.customerName }}
      </a>
    </ng-template>

    <ng-template #amountTemplate let-invoice>
      CHF {{ invoice.amount | number:'1.2-2' }}
    </ng-template>

    <ng-template #dueDateTemplate let-invoice>
      {{ invoice.dueDate | date:'shortDate' }}
    </ng-template>

    <ng-template #statusTemplate let-invoice>
         <span class="px-2 py-1 rounded text-xs font-semibold {{statusColor(invoice.status)}}">
          {{ ('invoice.status.' + invoice.status.toLocaleLowerCase()) |translate }}
        </span>
    </ng-template>

    <ng-template #installmentTemplate let-invoice>
      @if (invoice.isInstallment) {
        <span>{{ 'invoice.installment.yes' | translate: {number: invoice.installmentNumber} }}</span>
      }
      @if (!invoice.isInstallment) {
        <span>{{ 'invoice.installment.no' | translate }}</span>
      }
    </ng-template>
  `
})
export class InvoiceList implements AfterViewInit {
  private invoiceService = inject(Invoices);
  private router = inject(Router);

  @ViewChild('invoiceIdTemplate', { static: true }) invoiceIdTemplate!: TemplateRef<any>;
  @ViewChild('customerNameTemplate', { static: true }) customerNameTemplate!: TemplateRef<any>;
  @ViewChild('amountTemplate', { static: true }) amountTemplate!: TemplateRef<any>;
  @ViewChild('dueDateTemplate', { static: true }) dueDateTemplate!: TemplateRef<any>;
  @ViewChild('statusTemplate', { static: true }) statusTemplate!: TemplateRef<any>;
  @ViewChild('installmentTemplate', { static: true }) installmentTemplate!: TemplateRef<any>;

  invoices = signal<InvoiceView[]>([]);
  searchTerm = signal('');
  sortColumn = signal<SortColumn>('dueDate');
  sortDirection = signal<'asc' | 'desc'>('desc');
  showCancelModal = signal(false);
  selectedInvoiceId = signal<string | null>(null);
  cancelReason = signal('');
  statusColor = (status: string) => computed(() => {
    switch (status) {
      case 'OPEN':
        return 'bg-blue-100 text-blue-800';
      case 'PAID':
        return 'bg-green-100 text-green-800';
      case 'OVERDUE':
        return 'bg-orange-100 text-orange-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return '';
    }
  });

  columns: ColumnDefinition<InvoiceView>[] = [];

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.columns = [
        {
          key: 'invoiceId',
          headerKey: 'invoice.table.invoiceId',
          sortable: true,
          template: this.invoiceIdTemplate
        },
        {
          key: 'customerName',
          headerKey: 'invoice.table.customerName',
          sortable: true,
          template: this.customerNameTemplate
        },
        {
          key: 'amount',
          headerKey: 'invoice.table.amount',
          sortable: true,
          template: this.amountTemplate
        },
        {
          key: 'dueDate',
          headerKey: 'invoice.table.dueDate',
          sortable: true,
          template: this.dueDateTemplate
        },
        {
          key: 'status',
          headerKey: 'invoice.table.status',
          sortable: true,
          template: this.statusTemplate,
        },
        {
          key: 'isInstallment',
          headerKey: 'invoice.table.installment',
          sortable: false,
          template: this.installmentTemplate
        }
      ];
    });
  }

  rowActions: RowAction<InvoiceView>[] = [
    {
      labelKey: 'button.markPaid',
      onClick: (invoice) => this.markAsPaid(invoice.invoiceId),
      isDisabled: (invoice) => invoice.status !== 'OPEN' && invoice.status !== 'OVERDUE',
      stopPropagation: true
    },
    {
      labelKey: 'button.markOverdue',
      onClick: (invoice) => this.markAsOverdue(invoice.invoiceId),
      isDisabled: (invoice) => invoice.status !== 'OPEN',
      stopPropagation: true
    },
    {
      labelKey: 'common.cancel',
      onClick: (invoice) => this.openCancelModal(invoice.invoiceId),
      isDisabled: (invoice) => invoice.status === 'PAID' || invoice.status === 'CANCELLED',
      stopPropagation: true
    }
  ];

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
      let aVal: string | number = a[column];
      let bVal: string | number = b[column];

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

  constructor() {
    effect(() => {
      this.loadInvoices();
    }, { allowSignalWrites: true });
  }

  loadInvoices(): void {
    this.invoiceService.getAllInvoices().subscribe({
      next: (invoices) => this.invoices.set(invoices),
      error: (err) => console.error(err)
    });
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

  onSortChange(event: { column: string, direction: 'asc' | 'desc' }): void {
    this.sortColumn.set(event.column as SortColumn);
    this.sortDirection.set(event.direction);
  }

  trackByInvoiceId(index: number, invoice: InvoiceView): string {
    return invoice.invoiceId;
  }
}

import { Injectable, inject } from '@angular/core';
import { InvoicesService } from '../generated-api/api/invoices.service';
import { Observable } from 'rxjs';
import { InvoiceDto, CancelInvoiceRequest } from '../generated-api/model/models';

export type InvoiceView = InvoiceDto;

@Injectable({
  providedIn: 'root'
})
export class Invoices {
  private invoicesService = inject(InvoicesService);

  getAllInvoices(): Observable<InvoiceView[]> {
    return this.invoicesService.getInvoices();
  }

  getInvoicesByCustomerId(customerId: string): Observable<InvoiceView[]> {
    return this.invoicesService.getInvoicesByCustomerId(customerId);
  }

  getInvoiceById(invoiceId: string): Observable<InvoiceView> {
    return this.invoicesService.getInvoiceById(invoiceId);
  }

  markInvoiceAsPaid(invoiceId: string): Observable<void> {
    return this.invoicesService.markAsPaid(invoiceId);
  }

  markInvoiceAsOverdue(invoiceId: string): Observable<void> {
    return this.invoicesService.markAsOverdue(invoiceId);
  }

  cancelInvoice(invoiceId: string, reason: string): Observable<void> {
    const request: CancelInvoiceRequest = { reason };
    return this.invoicesService.cancelInvoice(invoiceId, request);
  }
}

import {Injectable, inject} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

export interface InvoiceView {
  invoiceId: string;
  customerId: string;
  customerName: string;
  bookingId: string;
  amount: number;
  dueDate: string;
  status: string;
  isInstallment: boolean;
  installmentNumber?: number;
  paidAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class Invoices {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/invoices';

  getAllInvoices(): Observable<InvoiceView[]> {
    return this.http.get<InvoiceView[]>(this.apiUrl);
  }

  getInvoicesByCustomerId(customerId: string): Observable<InvoiceView[]> {
    return this.http.get<InvoiceView[]>(`${this.apiUrl}/customer/${customerId}`);
  }

  getInvoiceById(invoiceId: string): Observable<InvoiceView> {
    return this.http.get<InvoiceView>(`${this.apiUrl}/${invoiceId}`);
  }

  markInvoiceAsPaid(invoiceId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${invoiceId}/pay`, {});
  }

  markInvoiceAsOverdue(invoiceId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${invoiceId}/mark-overdue`, {});
  }

  cancelInvoice(invoiceId: string, reason: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${invoiceId}/cancel`, { reason });
  }
}

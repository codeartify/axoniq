import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

export interface InvoiceView {
  invoiceId: string;
  customerId: string;
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
  private apiUrl = 'http://localhost:8080/api/invoices';

  constructor(private http: HttpClient) {}

  getInvoicesByCustomerId(customerId: string): Observable<InvoiceView[]> {
    return this.http.get<InvoiceView[]>(`${this.apiUrl}/customer/${customerId}`);
  }

  markInvoiceAsPaid(invoiceId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${invoiceId}/pay`, {});
  }
}

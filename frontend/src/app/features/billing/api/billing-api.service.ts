import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface InvoiceDto {
  invoiceId: string;
  customerId: string;
  bookingId: string;
  amount: number;
  dueDate: string;
  status: 'OPEN' | 'PAID' | 'OVERDUE' | 'CANCELLED';
  isInstallment: boolean;
  installmentNumber: number | null;
  paidAt: string | null;
}

@Injectable({ providedIn: 'root' })
export class BillingApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/invoices';

  getInvoices(status?: string): Observable<InvoiceDto[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<InvoiceDto[]>(this.baseUrl, { params });
  }

  getInvoiceById(invoiceId: string): Observable<InvoiceDto> {
    return this.http.get<InvoiceDto>(`${this.baseUrl}/${invoiceId}`);
  }

  markAsPaid(invoiceId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${invoiceId}/pay`, {});
  }
}

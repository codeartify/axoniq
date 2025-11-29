import {Injectable, inject} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

export enum PaymentMode {
  PAY_ON_SITE = 'PAY_ON_SITE',
  INVOICE_EMAIL = 'INVOICE_EMAIL'
}

export interface MembershipSignUpRequest {
  customerId: string;
  customerName: string;
  customerEmail: string;
  productVariantId: string;
  price: number;
  durationMonths: number;
  paymentMode: PaymentMode;
}

export interface MembershipSignUpResult {
  contractId: string;
  bookingId: string;
  invoiceId: string;
}

@Injectable({
  providedIn: 'root'
})
export class Memberships {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/memberships';

  signUp(request: MembershipSignUpRequest): Observable<MembershipSignUpResult> {
    return this.http.post<MembershipSignUpResult>(`${this.apiUrl}/sign-up`, request);
  }
}

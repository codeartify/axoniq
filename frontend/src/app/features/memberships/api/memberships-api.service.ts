import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProductVariantDto {
  id: string;
  code: string;
  name: string;
  productType: string;
  price: number;
  durationMonths: number;
}

export interface MembershipSignUpRequest {
  customerId: string;
  customerName: string;
  customerEmail: string;
  productVariantId: string;
  price: number;
  durationMonths: number;
  paymentMode: 'PAY_ON_SITE' | 'INVOICE_EMAIL';
}

export interface MembershipSignUpResult {
  contractId: string;
  bookingId: string;
  invoiceId: string;
}

@Injectable({ providedIn: 'root' })
export class MembershipsApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  getMembershipVariants(): Observable<ProductVariantDto[]> {
    return this.http.get<ProductVariantDto[]>(`${this.baseUrl}/products/memberships`);
  }

  signUp(request: MembershipSignUpRequest): Observable<MembershipSignUpResult> {
    return this.http.post<MembershipSignUpResult>(`${this.baseUrl}/memberships/sign-up`, request);
  }
}

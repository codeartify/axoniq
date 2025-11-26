import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Address {
  street: string;
  houseNumber: string;
  postalCode: string;
  city: string;
  country: string;
}

export interface RegisterCustomerRequest {
  salutation: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  address: Address;
  email: string;
  phoneNumber?: string;
}

export interface UpdateCustomerRequest {
  salutation: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  address: Address;
  email: string;
  phoneNumber?: string;
}

export interface CustomerRegistrationResponse {
  customerId?: string;
  error?: string;
}

export interface CustomerView {
  customerId: string;
  salutation: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  address: Address;
  email: string;
  phoneNumber?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private apiUrl = 'http://localhost:8080/api/customers';

  constructor(private http: HttpClient) {}

  registerCustomer(request: RegisterCustomerRequest): Observable<CustomerRegistrationResponse> {
    return this.http.post<CustomerRegistrationResponse>(this.apiUrl, request);
  }

  getCustomer(customerId: string): Observable<CustomerView> {
    return this.http.get<CustomerView>(`${this.apiUrl}/${customerId}`);
  }

  getAllCustomers(): Observable<CustomerView[]> {
    return this.http.get<CustomerView[]>(this.apiUrl);
  }

  updateCustomer(customerId: string, request: UpdateCustomerRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${customerId}`, request);
  }
}

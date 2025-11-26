import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

export interface ProductBehaviorConfig {
  isTimeBased: boolean;
  isSessionBased: boolean;
  canBePaused: boolean;
  autoRenew: boolean;
  renewalLeadTimeDays?: number;
  contributesToMembershipStatus: boolean;
  maxActivePerCustomer?: number;
  exclusivityGroup?: string;
}

export interface CreateProductRequest {
  code: string;
  name: string;
  productType: string;
  audience: string;
  requiresMembership: boolean;
  price: number;
  behavior: ProductBehaviorConfig;
}

export interface UpdateProductRequest {
  code: string;
  name: string;
  productType: string;
  audience: string;
  requiresMembership: boolean;
  price: number;
  behavior: ProductBehaviorConfig;
}

export interface ProductCreationResponse {
  productId?: string;
  error?: string;
}

export interface ProductView {
  productId: string;
  code: string;
  name: string;
  productType: string;
  audience: string;
  requiresMembership: boolean;
  price: number;
  behavior: ProductBehaviorConfig;
}

@Injectable({
  providedIn: 'root'
})
export class Products {
  private apiUrl = 'http://localhost:8080/api/products';

  constructor(private http: HttpClient) {}

  createProduct(request: CreateProductRequest): Observable<ProductCreationResponse> {
    return this.http.post<ProductCreationResponse>(this.apiUrl, request);
  }

  getProduct(productId: string): Observable<ProductView> {
    return this.http.get<ProductView>(`${this.apiUrl}/${productId}`);
  }

  getAllProducts(): Observable<ProductView[]> {
    return this.http.get<ProductView[]>(this.apiUrl);
  }

  updateProduct(productId: string, request: UpdateProductRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${productId}`, request);
  }
}

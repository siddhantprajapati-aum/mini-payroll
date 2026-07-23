import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PayrollResponse } from '../models/payroll.model';

@Injectable({ providedIn: 'root' })
export class PayrollService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/payroll`;

  getPayroll(employeeId: number, year: number, month: number): Observable<PayrollResponse> {
    const params = new HttpParams().set('year', year).set('month', month);
    return this.http.get<PayrollResponse>(`${this.baseUrl}/${employeeId}`, { params });
  }
}

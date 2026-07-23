import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Employee, EmployeeRequest } from '../models/employee.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/employees`;

  getAll(page = 0, size = 50): Observable<Page<Employee>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Employee>>(this.baseUrl, { params });
  }

  create(request: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>(this.baseUrl, request);
  }
}

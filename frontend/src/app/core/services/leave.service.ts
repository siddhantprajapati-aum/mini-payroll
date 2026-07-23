import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Leave, LeaveRequest } from '../models/leave.model';
import { LeaveStatus } from '../models/enums';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class LeaveService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/leaves`;

  apply(employeeId: number, request: LeaveRequest): Observable<Leave> {
    return this.http.post<Leave>(`${this.baseUrl}/${employeeId}`, request);
  }

  updateStatus(leaveId: number, status: LeaveStatus): Observable<Leave> {
    const params = new HttpParams().set('status', status);
    return this.http.put<Leave>(`${this.baseUrl}/${leaveId}/status`, null, { params });
  }

  getAll(page = 0, size = 50): Observable<Page<Leave>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Leave>>(this.baseUrl, { params });
  }

  getByEmployee(employeeId: number, page = 0, size = 50): Observable<Page<Leave>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Leave>>(`${this.baseUrl}/employee/${employeeId}`, { params });
  }
}

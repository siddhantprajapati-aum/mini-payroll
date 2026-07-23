import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Attendance,
  AttendanceRequest,
  BulkAttendanceRequest
} from '../models/attendance.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class AttendanceService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/attendances`;

  mark(employeeId: number, request: AttendanceRequest): Observable<Attendance> {
    return this.http.post<Attendance>(`${this.baseUrl}/${employeeId}`, request);
  }

  markBulk(employeeId: number, request: BulkAttendanceRequest): Observable<Attendance[]> {
    return this.http.post<Attendance[]>(`${this.baseUrl}/${employeeId}/bulk`, request);
  }

  getByEmployee(employeeId: number, page = 0, size = 50): Observable<Page<Attendance>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Attendance>>(`${this.baseUrl}/${employeeId}`, { params });
  }
}

import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, finalize, map, of, shareReplay, switchMap, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Employee, EmployeeRequest } from '../models/employee.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/employees`;
  private readonly cache = new Map<string, Page<Employee>>();
  private readonly inflight = new Map<string, Observable<Page<Employee>>>();

  getAll(page = 0, size = 50): Observable<Page<Employee>> {
    const key = this.cacheKey(page, size);
    const cached = this.cache.get(key);
    if (cached) {
      return of(cached);
    }

    const pending = this.inflight.get(key);
    if (pending) {
      return pending;
    }

    const request$ = this.http
      .get<Page<Employee>>(this.baseUrl, {
        params: new HttpParams().set('page', page).set('size', size)
      })
      .pipe(
        tap((result) => this.cache.set(key, result)),
        finalize(() => this.inflight.delete(key)),
        shareReplay(1)
      );

    this.inflight.set(key, request$);
    return request$;
  }

  create(request: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>(this.baseUrl, request).pipe(
      switchMap((employee) => {
        this.clearCache();
        return this.getAll().pipe(map(() => employee));
      })
    );
  }

  private clearCache(): void {
    this.cache.clear();
    this.inflight.clear();
  }

  private cacheKey(page: number, size: number): string {
    return `${page}:${size}`;
  }
}

import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { EmployeeService } from '../../core/services/employee.service';
import { PayrollService } from '../../core/services/payroll.service';
import { Employee } from '../../core/models/employee.model';
import { PayrollResponse } from '../../core/models/payroll.model';
import { extractApiError } from '../../core/utils/api-error';

@Component({
  selector: 'app-payroll-page',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe],
  templateUrl: './payroll-page.component.html',
  styleUrl: './payroll-page.component.scss'
})
export class PayrollPageComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly payrollService = inject(PayrollService);
  private readonly fb = inject(FormBuilder);

  readonly employees = signal<Employee[]>([]);
  readonly payroll = signal<PayrollResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly now = new Date();
  readonly months = Array.from({ length: 12 }, (_, i) => i + 1);
  readonly years = Array.from({ length: 6 }, (_, i) => this.now.getFullYear() - 2 + i);

  readonly form = this.fb.nonNullable.group({
    employeeId: ['', Validators.required],
    year: [String(this.now.getFullYear()), Validators.required],
    month: [String(this.now.getMonth() + 1), Validators.required]
  });

  ngOnInit(): void {
    this.employeeService.getAll().subscribe({
      next: (page) => this.employees.set(page.content),
      error: (err) => this.error.set(extractApiError(err, 'Failed to load employees.'))
    });
  }

  monthLabel(month: number): string {
    return new Date(2000, month - 1, 1).toLocaleString(undefined, { month: 'long' });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Please select an employee, year, and month.');
      return;
    }

    const { employeeId, year, month } = this.form.getRawValue();

    this.loading.set(true);
    this.error.set(null);
    this.payroll.set(null);

    this.payrollService.getPayroll(Number(employeeId), Number(year), Number(month)).subscribe({
      next: (result) => {
        this.payroll.set(result);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractApiError(err, 'Failed to generate payroll.'));
        this.loading.set(false);
      }
    });
  }
}

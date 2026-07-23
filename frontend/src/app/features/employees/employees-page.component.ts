import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { EmployeeService } from '../../core/services/employee.service';
import { Employee } from '../../core/models/employee.model';
import { ROLES, SALARY_TYPES } from '../../core/models/enums';
import { extractApiError } from '../../core/utils/api-error';

@Component({
  selector: 'app-employees-page',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  templateUrl: './employees-page.component.html'
})
export class EmployeesPageComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly fb = inject(FormBuilder);

  readonly roles = ROLES;
  readonly salaryTypes = SALARY_TYPES;
  readonly employees = signal<Employee[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    role: this.fb.nonNullable.control<(typeof ROLES)[number]>('OFFICE', Validators.required),
    salaryType: this.fb.nonNullable.control<(typeof SALARY_TYPES)[number]>('MONTHLY', Validators.required),
    salaryAmount: [null as number | null, [Validators.required, Validators.min(0.01)]]
  });

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.loading.set(true);
    this.error.set(null);
    this.employeeService.getAll().subscribe({
      next: (page) => {
        this.employees.set(page.content);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractApiError(err, 'Failed to load employees.'));
        this.loading.set(false);
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Please enter a valid name and salary amount greater than 0.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    const value = this.form.getRawValue();
    this.employeeService
      .create({
        name: value.name.trim(),
        role: value.role,
        salaryType: value.salaryType,
        salaryAmount: Number(value.salaryAmount)
      })
      .subscribe({
        next: () => {
          this.success.set('Employee created successfully.');
          this.form.reset({
            name: '',
            role: 'OFFICE',
            salaryType: 'MONTHLY',
            salaryAmount: null
          });
          this.saving.set(false);
          this.loadEmployees();
        },
        error: (err) => {
          this.error.set(extractApiError(err, 'Failed to create employee.'));
          this.saving.set(false);
        }
      });
  }
}

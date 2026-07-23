import { Component, OnInit, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { EmployeeService } from '../../core/services/employee.service';
import { LeaveService } from '../../core/services/leave.service';
import { Employee } from '../../core/models/employee.model';
import { Leave } from '../../core/models/leave.model';
import { LeaveStatus } from '../../core/models/enums';
import { extractApiError } from '../../core/utils/api-error';
import {
  allowsWeekendWork,
  dayOfWeekLabel,
  isPastDate,
  isWeekend,
  nearestFutureOrTodayWeekday,
  toLocalDateString
} from '../../core/utils/date-rules';

@Component({
  selector: 'app-leave-page',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './leave-page.component.html'
})
export class LeavePageComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly leaveService = inject(LeaveService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly employees = signal<Employee[]>([]);
  readonly leaves = signal<Leave[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly updatingId = signal<number | null>(null);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly startNotice = signal<string | null>(null);
  readonly endNotice = signal<string | null>(null);

  readonly today = toLocalDateString();
  readonly defaultWorkday = nearestFutureOrTodayWeekday();

  readonly form = this.fb.nonNullable.group({
    employeeId: ['', Validators.required],
    startDate: [this.defaultWorkday, Validators.required],
    endDate: [this.defaultWorkday, Validators.required],
    reason: ['', [Validators.required, Validators.minLength(3)]]
  });

  ngOnInit(): void {
    this.employeeService.getAll().subscribe({
      next: (page) => this.employees.set(page.content),
      error: (err) => this.error.set(extractApiError(err, 'Failed to load employees.'))
    });
    this.loadLeaves();

    this.form.controls.employeeId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.startNotice.set(null);
        this.endNotice.set(null);
        this.enforceLeaveDatePolicy('start');
        this.enforceLeaveDatePolicy('end');
      });

    this.form.controls.startDate.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.enforceLeaveDatePolicy('start'));

    this.form.controls.endDate.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.enforceLeaveDatePolicy('end'));
  }

  findEmployee(id: string | number | null | undefined): Employee | null {
    if (id === null || id === undefined || id === '') {
      return null;
    }
    const numericId = Number(id);
    return this.employees().find((employee) => employee.id === numericId) ?? null;
  }

  allowsWeekendsForSelected(): boolean {
    return allowsWeekendWork(this.findEmployee(this.form.controls.employeeId.value)?.salaryType);
  }

  private enforceLeaveDatePolicy(which: 'start' | 'end'): void {
    const employeeId = this.form.controls.employeeId.value;
    const control = which === 'start' ? this.form.controls.startDate : this.form.controls.endDate;
    const notice = which === 'start' ? this.startNotice : this.endNotice;
    const date = control.value;

    if (!employeeId || !date || !isWeekend(date)) {
      notice.set(null);
      return;
    }

    if (allowsWeekendWork(this.findEmployee(employeeId)?.salaryType)) {
      notice.set(`${dayOfWeekLabel(date)} is a weekend — allowed for daily wage employees.`);
      return;
    }

    const corrected = nearestFutureOrTodayWeekday(new Date(`${date}T00:00:00`));
    notice.set(
      `${dayOfWeekLabel(date)} is a weekend. Monthly employees cannot ${which === 'start' ? 'start' : 'end'} leave on weekends.`
    );
    if (corrected !== date) {
      control.setValue(corrected, { emitEvent: false });
    }
  }

  loadLeaves(): void {
    this.loading.set(true);
    this.leaveService.getAll().subscribe({
      next: (page) => {
        this.leaves.set(page.content);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractApiError(err, 'Failed to load leaves.'));
        this.loading.set(false);
      }
    });
  }

  submit(): void {
    this.enforceLeaveDatePolicy('start');
    this.enforceLeaveDatePolicy('end');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Please select an employee, valid dates, and a reason (min 3 characters).');
      return;
    }

    const { employeeId, startDate, endDate, reason } = this.form.getRawValue();
    const numericId = Number(employeeId);
    const allowWeekends = allowsWeekendWork(this.findEmployee(numericId)?.salaryType);

    if (startDate > endDate) {
      this.error.set('Leave start date cannot be after end date.');
      return;
    }

    if (isPastDate(startDate, this.today) || isPastDate(endDate, this.today)) {
      this.error.set('Leave dates cannot be in the past.');
      return;
    }

    if (!allowWeekends && (isWeekend(startDate) || isWeekend(endDate))) {
      this.error.set('Leave cannot start or end on a weekend for monthly employees.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    this.leaveService.apply(numericId, { startDate, endDate, reason: reason.trim() }).subscribe({
      next: () => {
        this.success.set('Leave applied successfully.');
        this.form.patchValue({ reason: '' });
        this.saving.set(false);
        this.loadLeaves();
      },
      error: (err) => {
        this.error.set(extractApiError(err, 'Failed to apply leave.'));
        this.saving.set(false);
      }
    });
  }

  updateStatus(leaveId: number, status: LeaveStatus): void {
    this.updatingId.set(leaveId);
    this.error.set(null);
    this.success.set(null);

    this.leaveService.updateStatus(leaveId, status).subscribe({
      next: () => {
        this.success.set(`Leave ${status.toLowerCase()}.`);
        this.updatingId.set(null);
        this.loadLeaves();
      },
      error: (err) => {
        this.error.set(extractApiError(err, 'Failed to update leave status.'));
        this.updatingId.set(null);
      }
    });
  }
}

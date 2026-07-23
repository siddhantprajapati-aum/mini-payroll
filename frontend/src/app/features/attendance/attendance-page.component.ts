import { Component, OnInit, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { EmployeeService } from '../../core/services/employee.service';
import { AttendanceService } from '../../core/services/attendance.service';
import { Employee } from '../../core/models/employee.model';
import { Attendance, AttendanceRequest } from '../../core/models/attendance.model';
import { ATTENDANCE_STATUSES, AttendanceStatus } from '../../core/models/enums';
import { extractApiError } from '../../core/utils/api-error';
import {
  allowsWeekendWork,
  dayOfWeekLabel,
  eachDateInclusive,
  eachWeekdayInclusive,
  isFutureDate,
  isWeekend,
  nearestPastOrTodayWeekday,
  toLocalDateString
} from '../../core/utils/date-rules';

@Component({
  selector: 'app-attendance-page',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './attendance-page.component.html'
})
export class AttendancePageComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly attendanceService = inject(AttendanceService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly statuses = ATTENDANCE_STATUSES;
  readonly today = toLocalDateString();
  readonly defaultWorkday = nearestPastOrTodayWeekday();
  readonly employees = signal<Employee[]>([]);
  readonly history = signal<Attendance[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly bulkSaving = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly dateNotice = signal<string | null>(null);
  readonly bulkNotice = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    employeeId: ['', Validators.required],
    date: [this.defaultWorkday, Validators.required],
    status: this.fb.nonNullable.control<AttendanceStatus>('PRESENT', Validators.required),
    remarks: ['']
  });

  readonly bulkForm = this.fb.nonNullable.group({
    employeeId: ['', Validators.required],
    startDate: [this.defaultWorkday, Validators.required],
    endDate: [this.defaultWorkday, Validators.required],
    status: this.fb.nonNullable.control<AttendanceStatus>('PRESENT', Validators.required),
    remarks: ['']
  });

  ngOnInit(): void {
    this.employeeService.getAll().subscribe({
      next: (page) => this.employees.set(page.content),
      error: (err) => this.error.set(extractApiError(err, 'Failed to load employees.'))
    });

    this.form.controls.employeeId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((id) => {
        if (id && this.bulkForm.controls.employeeId.value !== id) {
          this.bulkForm.controls.employeeId.setValue(id, { emitEvent: false });
        }
        this.onEmployeeChanged(id);
        this.enforceSingleDatePolicy();
      });

    this.bulkForm.controls.employeeId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((id) => {
        if (id && this.form.controls.employeeId.value !== id) {
          this.form.controls.employeeId.setValue(id, { emitEvent: false });
        }
        this.onEmployeeChanged(id);
        this.enforceBulkDatePolicy();
      });

    this.form.controls.date.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.enforceSingleDatePolicy());

    this.bulkForm.controls.startDate.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.enforceBulkDatePolicy());

    this.bulkForm.controls.endDate.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.enforceBulkDatePolicy());
  }

  findEmployee(id: string | number | null | undefined): Employee | null {
    if (id === null || id === undefined || id === '') {
      return null;
    }
    const numericId = Number(id);
    return this.employees().find((employee) => employee.id === numericId) ?? null;
  }

  isWeekendDate(date: string): boolean {
    return !!date && isWeekend(date);
  }

  allowsWeekendsForSelected(useBulk = false): boolean {
    const id = useBulk
      ? this.bulkForm.controls.employeeId.value
      : this.form.controls.employeeId.value;
    return this.employeeAllowsWeekends(id);
  }

  private onEmployeeChanged(id: string): void {
    this.dateNotice.set(null);
    this.bulkNotice.set(null);
    if (!id) {
      this.history.set([]);
      return;
    }
    this.loadHistory(Number(id));
  }

  private employeeAllowsWeekends(id: string | number | null | undefined): boolean {
    return allowsWeekendWork(this.findEmployee(id)?.salaryType);
  }

  private enforceSingleDatePolicy(): void {
    const employeeId = this.form.controls.employeeId.value;
    const date = this.form.controls.date.value;
    if (!employeeId || !date || !isWeekend(date)) {
      if (!this.isWeekendDate(date)) {
        this.dateNotice.set(null);
      }
      return;
    }

    if (this.employeeAllowsWeekends(employeeId)) {
      this.dateNotice.set(
        `${dayOfWeekLabel(date)} is a weekend — allowed for daily wage employees.`
      );
      return;
    }

    const corrected = nearestPastOrTodayWeekday(new Date(`${date}T00:00:00`));
    this.dateNotice.set(
      `${dayOfWeekLabel(date)} is a weekend. Monthly employees cannot mark attendance on weekends.`
    );
    if (corrected !== date) {
      this.form.controls.date.setValue(corrected, { emitEvent: false });
    }
  }

  private enforceBulkDatePolicy(): void {
    const employeeId = this.bulkForm.controls.employeeId.value;
    if (!employeeId) {
      this.bulkNotice.set(null);
      return;
    }

    const allowWeekends = this.employeeAllowsWeekends(employeeId);
    let start = this.bulkForm.controls.startDate.value;
    let end = this.bulkForm.controls.endDate.value;

    if (!allowWeekends) {
      if (start && isWeekend(start)) {
        const corrected = nearestPastOrTodayWeekday(new Date(`${start}T00:00:00`));
        this.bulkNotice.set(
          `${dayOfWeekLabel(start)} start date is a weekend — reset for monthly employee.`
        );
        this.bulkForm.controls.startDate.setValue(corrected, { emitEvent: false });
        start = corrected;
      }
      if (end && isWeekend(end)) {
        const corrected = nearestPastOrTodayWeekday(new Date(`${end}T00:00:00`));
        this.bulkNotice.set(
          `${dayOfWeekLabel(end)} end date is a weekend — reset for monthly employee.`
        );
        this.bulkForm.controls.endDate.setValue(corrected, { emitEvent: false });
        end = corrected;
      }
      if (
        start &&
        end &&
        !isWeekend(start) &&
        !isWeekend(end) &&
        !this.bulkNotice()?.includes('weekend')
      ) {
        const hasWeekendInRange = eachDateInclusive(start, end).some((date) => isWeekend(date));
        this.bulkNotice.set(
          hasWeekendInRange
            ? 'Monthly employee — Saturdays and Sundays in this range will be skipped.'
            : null
        );
      }
      return;
    }

    this.bulkNotice.set(
      start && end && eachDateInclusive(start, end).some((date) => isWeekend(date))
        ? 'Daily wage employee — weekends in this range will be included.'
        : null
    );
  }

  loadHistory(employeeId: number): void {
    this.loading.set(true);
    this.attendanceService.getByEmployee(employeeId).subscribe({
      next: (page) => {
        this.history.set(page.content);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractApiError(err, 'Failed to load attendance.'));
        this.loading.set(false);
      }
    });
  }

  submit(): void {
    this.enforceSingleDatePolicy();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Please select an employee and a valid date.');
      return;
    }

    const { employeeId, date, status, remarks } = this.form.getRawValue();
    const numericId = Number(employeeId);
    const allowWeekends = this.employeeAllowsWeekends(numericId);

    if (isFutureDate(date, this.today)) {
      this.error.set('Attendance cannot be marked for a future date.');
      return;
    }

    if (!allowWeekends && isWeekend(date)) {
      this.error.set(
        `${dayOfWeekLabel(date)} is a weekend. Monthly employees cannot mark attendance on weekends.`
      );
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    this.attendanceService
      .mark(numericId, {
        date,
        status,
        remarks: remarks?.trim() ? remarks.trim() : undefined
      })
      .subscribe({
        next: () => {
          this.success.set('Attendance marked successfully.');
          this.saving.set(false);
          this.loadHistory(numericId);
        },
        error: (err) => {
          this.error.set(extractApiError(err, 'Failed to mark attendance.'));
          this.saving.set(false);
        }
      });
  }

  submitBulk(): void {
    this.enforceBulkDatePolicy();

    if (this.bulkForm.invalid) {
      this.bulkForm.markAllAsTouched();
      this.error.set('Please select an employee and a valid date range.');
      return;
    }

    const { employeeId, startDate, endDate, status, remarks } = this.bulkForm.getRawValue();
    const numericId = Number(employeeId);
    const allowWeekends = this.employeeAllowsWeekends(numericId);

    if (startDate > endDate) {
      this.error.set('Bulk start date cannot be after end date.');
      return;
    }

    if (isFutureDate(startDate, this.today) || isFutureDate(endDate, this.today)) {
      this.error.set('Bulk attendance cannot include future dates.');
      return;
    }

    if (!allowWeekends && (isWeekend(startDate) || isWeekend(endDate))) {
      this.error.set('Weekend start/end is not allowed for monthly employees.');
      return;
    }

    const dates = (allowWeekends ? eachDateInclusive(startDate, endDate) : eachWeekdayInclusive(startDate, endDate))
      .filter((date) => !isFutureDate(date, this.today));

    if (dates.length === 0) {
      this.error.set('No valid dates found in the selected range.');
      return;
    }

    const note = remarks?.trim() ? remarks.trim() : undefined;
    const records: AttendanceRequest[] = dates.map((date) => ({ date, status, remarks: note }));

    this.bulkSaving.set(true);
    this.error.set(null);
    this.success.set(null);

    this.attendanceService.markBulk(numericId, { records }).subscribe({
      next: (saved) => {
        this.success.set(
          allowWeekends
            ? `Bulk attendance saved for ${saved.length} day(s).`
            : `Bulk attendance saved for ${saved.length} weekday(s). Weekends were skipped.`
        );
        this.bulkSaving.set(false);
        this.loadHistory(numericId);
      },
      error: (err) => {
        this.error.set(extractApiError(err, 'Failed to save bulk attendance.'));
        this.bulkSaving.set(false);
      }
    });
  }
}

import { Routes } from '@angular/router';
import { ShellComponent } from './layout/shell/shell.component';

export const routes: Routes = [
  {
    path: '',
    component: ShellComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'employees' },
      {
        path: 'employees',
        loadComponent: () =>
          import('./features/employees/employees-page.component').then(
            (m) => m.EmployeesPageComponent
          )
      },
      {
        path: 'attendance',
        loadComponent: () =>
          import('./features/attendance/attendance-page.component').then(
            (m) => m.AttendancePageComponent
          )
      },
      {
        path: 'leave',
        loadComponent: () =>
          import('./features/leave/leave-page.component').then((m) => m.LeavePageComponent)
      },
      {
        path: 'payroll',
        loadComponent: () =>
          import('./features/payroll/payroll-page.component').then((m) => m.PayrollPageComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'employees' }
];

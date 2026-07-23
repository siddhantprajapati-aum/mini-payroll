import { LeaveStatus } from './enums';
import { Employee } from './employee.model';

export interface Leave {
  id: number;
  employee: Employee;
  startDate: string;
  endDate: string;
  reason: string;
  status: LeaveStatus;
  appliedAt?: string;
  reviewedAt?: string;
}

export interface LeaveRequest {
  startDate: string;
  endDate: string;
  reason: string;
}

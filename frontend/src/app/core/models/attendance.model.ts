import { AttendanceStatus } from './enums';
import { Employee } from './employee.model';

export interface Attendance {
  id: number;
  employee: Employee;
  date: string;
  status: AttendanceStatus;
  remarks?: string;
  createdAt?: string;
}

export interface AttendanceRequest {
  date: string;
  status: AttendanceStatus;
  remarks?: string;
}

export interface BulkAttendanceRequest {
  records: AttendanceRequest[];
}

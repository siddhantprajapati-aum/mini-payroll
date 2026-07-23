export type Role = 'WFH' | 'OFFICE' | 'ONSITE';
export type SalaryType = 'MONTHLY' | 'DAILY';
export type AttendanceStatus = 'PRESENT' | 'ABSENT';
export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export const ROLES: Role[] = ['WFH', 'OFFICE', 'ONSITE'];
export const SALARY_TYPES: SalaryType[] = ['MONTHLY', 'DAILY'];
export const ATTENDANCE_STATUSES: AttendanceStatus[] = ['PRESENT', 'ABSENT'];
export const LEAVE_STATUSES: LeaveStatus[] = ['PENDING', 'APPROVED', 'REJECTED'];

import { Role, SalaryType } from './enums';

export interface Employee {
  id: number;
  name: string;
  role: Role;
  salaryType: SalaryType;
  salaryAmount: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface EmployeeRequest {
  name: string;
  role: Role;
  salaryType: SalaryType;
  salaryAmount: number;
}

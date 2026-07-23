export interface PayrollResponse {
  employeeId: number;
  employeeName: string;
  salaryType: string;
  baseSalaryAmount: number;
  year: number;
  month: number;
  workingDays: number;
  presentDays: number;
  absentDays: number;
  unmarkedDays: number;
  calculatedSalary: number;
  formula: string;
}

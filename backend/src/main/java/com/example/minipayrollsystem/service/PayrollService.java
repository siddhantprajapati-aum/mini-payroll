package com.example.minipayrollsystem.service;

import com.example.minipayrollsystem.dto.PayrollResponseDTO;
import com.example.minipayrollsystem.entity.Attendance;
import com.example.minipayrollsystem.entity.Employee;
import com.example.minipayrollsystem.enums.AttendanceStatus;
import com.example.minipayrollsystem.enums.SalaryType;
import com.example.minipayrollsystem.exception.ResourceNotFoundException;
import com.example.minipayrollsystem.repository.AttendanceRepository;
import com.example.minipayrollsystem.repository.EmployeeRepository;
import com.example.minipayrollsystem.utils.PayrollCalculatorUtil;
import com.example.minipayrollsystem.utils.WorkingDaysUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    public PayrollResponseDTO generatePayroll(Long employeeId, int year, int month) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Attendance> attendances = attendanceRepository
                .findByEmployeeIdAndDateBetween(employeeId, startDate, endDate);

        List<Attendance> countable = attendances.stream()
                .filter(attendance -> isCountableDay(employee.getSalaryType(), attendance.getDate()))
                .toList();

        long presentDays = countable.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long absentDays = countable.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long workingDays = WorkingDaysUtil.countExpectedDays(employee.getSalaryType(), yearMonth);
        long unmarkedDays = Math.max(0, workingDays - presentDays - absentDays);

        double calculatedSalary = PayrollCalculatorUtil.calculateSalary(
                employee.getSalaryType(),
                employee.getSalaryAmount(),
                presentDays
        );

        return PayrollResponseDTO.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .salaryType(employee.getSalaryType().name())
                .baseSalaryAmount(employee.getSalaryAmount())
                .year(year)
                .month(month)
                .workingDays(workingDays)
                .presentDays(presentDays)
                .absentDays(absentDays)
                .unmarkedDays(unmarkedDays)
                .calculatedSalary(calculatedSalary)
                .formula(PayrollCalculatorUtil.buildFormula(
                        employee.getSalaryType(),
                        employee.getSalaryAmount(),
                        presentDays
                ))
                .build();
    }

    private boolean isCountableDay(SalaryType salaryType, LocalDate date) {
        if (date == null) {
            return false;
        }
        if (salaryType == SalaryType.DAILY) {
            return true;
        }
        return !WorkingDaysUtil.isWeekend(date);
    }
}

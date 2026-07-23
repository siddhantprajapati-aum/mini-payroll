package com.example.minipayrollsystem.service;

import com.example.minipayrollsystem.dto.PayrollResponseDTO;
import com.example.minipayrollsystem.entity.Attendance;
import com.example.minipayrollsystem.entity.Employee;
import com.example.minipayrollsystem.enums.AttendanceStatus;
import com.example.minipayrollsystem.enums.SalaryType;
import com.example.minipayrollsystem.exception.ResourceNotFoundException;
import com.example.minipayrollsystem.repository.AttendanceRepository;
import com.example.minipayrollsystem.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private PayrollService payrollService;

    @Test
    void testGeneratePayroll_MonthlySalary() {
        Long employeeId = 1L;
        int year = 2026;
        int month = 7;

        Employee mockEmployee = new Employee();
        mockEmployee.setId(employeeId);
        mockEmployee.setName("Alice");
        mockEmployee.setSalaryType(SalaryType.MONTHLY);
        mockEmployee.setSalaryAmount(60000.0);

        Attendance a1 = new Attendance();
        a1.setDate(LocalDate.of(2026, 7, 1)); // Wednesday
        a1.setStatus(AttendanceStatus.PRESENT);

        Attendance a2 = new Attendance();
        a2.setDate(LocalDate.of(2026, 7, 2)); // Thursday
        a2.setStatus(AttendanceStatus.PRESENT);

        Attendance a3 = new Attendance();
        a3.setDate(LocalDate.of(2026, 7, 3)); // Friday
        a3.setStatus(AttendanceStatus.ABSENT);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate))
                .thenReturn(Arrays.asList(a1, a2, a3));

        PayrollResponseDTO result = payrollService.generatePayroll(employeeId, year, month);

        assertNotNull(result);
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals("Alice", result.getEmployeeName());
        assertEquals(23, result.getWorkingDays()); // July 2026 weekdays
        assertEquals(2, result.getPresentDays());
        assertEquals(1, result.getAbsentDays());
        assertEquals(20, result.getUnmarkedDays()); // 23 - 2 - 1
        assertEquals(4000.0, result.getCalculatedSalary());
        assertEquals("(60000.0 / 30) × 2", result.getFormula());
        assertEquals(year, result.getYear());
        assertEquals(month, result.getMonth());
    }

    @Test
    void testGeneratePayroll_MonthlyIgnoresWeekendAttendance() {
        Long employeeId = 1L;
        int year = 2026;
        int month = 7;

        Employee mockEmployee = new Employee();
        mockEmployee.setId(employeeId);
        mockEmployee.setName("Alice");
        mockEmployee.setSalaryType(SalaryType.MONTHLY);
        mockEmployee.setSalaryAmount(60000.0);

        Attendance weekday = new Attendance();
        weekday.setDate(LocalDate.of(2026, 7, 1));
        weekday.setStatus(AttendanceStatus.PRESENT);

        Attendance weekend = new Attendance();
        weekend.setDate(LocalDate.of(2026, 7, 4)); // Saturday
        weekend.setStatus(AttendanceStatus.PRESENT);

        YearMonth yearMonth = YearMonth.of(year, month);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(attendanceRepository.findByEmployeeIdAndDateBetween(
                employeeId, yearMonth.atDay(1), yearMonth.atEndOfMonth()))
                .thenReturn(Arrays.asList(weekday, weekend));

        PayrollResponseDTO result = payrollService.generatePayroll(employeeId, year, month);

        assertEquals(1, result.getPresentDays());
        assertEquals(22, result.getUnmarkedDays()); // 23 - 1
        assertEquals(2000.0, result.getCalculatedSalary());
    }

    @Test
    void testGeneratePayroll_DailyWage() {
        Long employeeId = 2L;
        int year = 2026;
        int month = 7;

        Employee mockEmployee = new Employee();
        mockEmployee.setId(employeeId);
        mockEmployee.setName("Bob");
        mockEmployee.setSalaryType(SalaryType.DAILY);
        mockEmployee.setSalaryAmount(500.0);

        Attendance a1 = new Attendance();
        a1.setDate(LocalDate.of(2026, 7, 1));
        a1.setStatus(AttendanceStatus.PRESENT);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate))
                .thenReturn(java.util.Collections.nCopies(15, a1));

        PayrollResponseDTO result = payrollService.generatePayroll(employeeId, year, month);

        assertEquals(31, result.getWorkingDays()); // July calendar days for daily
        assertEquals(15, result.getPresentDays());
        assertEquals(16, result.getUnmarkedDays()); // 31 - 15
        assertEquals(7500.0, result.getCalculatedSalary());
        assertEquals("500.0 × 15", result.getFormula());
    }

    @Test
    void testGeneratePayroll_EmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                payrollService.generatePayroll(99L, 2026, 7)
        );
    }
}

package com.example.minipayrollsystem.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayrollResponseDTO {
    private Long employeeId;
    private String employeeName;
    private String salaryType;
    private Double baseSalaryAmount;
    private int year;
    private int month;
    private long workingDays;
    private long presentDays;
    private long absentDays;
    private long unmarkedDays;
    private Double calculatedSalary;
    private String formula;
}

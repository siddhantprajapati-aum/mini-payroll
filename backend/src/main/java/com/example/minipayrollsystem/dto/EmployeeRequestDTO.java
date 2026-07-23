package com.example.minipayrollsystem.dto;

import com.example.minipayrollsystem.enums.Role;
import com.example.minipayrollsystem.enums.SalaryType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeRequestDTO {

    @NotBlank(message = "Employee name cannot be empty")
    private String name;

    @NotNull(message = "Role is required")
    private Role role;

    @NotNull(message = "Salary type is required")
    private SalaryType salaryType;

    @NotNull(message = "Salary amount is required")
    @Min(value = 0, message = "Salary cannot be negative")
    private Double salaryAmount;
}
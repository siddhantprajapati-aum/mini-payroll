package com.example.minipayrollsystem.utils;

import com.example.minipayrollsystem.constants.AppConstants;
import com.example.minipayrollsystem.enums.SalaryType;
import com.example.minipayrollsystem.exception.BadRequestException;

public final class PayrollCalculatorUtil {

    private PayrollCalculatorUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static double calculateSalary(SalaryType type, Double baseAmount, long presentDays) {
        if (type == null) {
            throw new BadRequestException("Salary type is required for payroll calculation");
        }
        if (baseAmount == null) {
            throw new BadRequestException("Base salary amount is required for payroll calculation");
        }

        double calculatedSalary;
        if (type == SalaryType.MONTHLY) {
            calculatedSalary = (baseAmount / AppConstants.DAYS_IN_PAYROLL_MONTH) * presentDays;
        } else if (type == SalaryType.DAILY) {
            calculatedSalary = baseAmount * presentDays;
        } else {
            throw new BadRequestException("Unsupported salary type: " + type);
        }

        return Math.round(calculatedSalary * 100.0) / 100.0;
    }

    public static String buildFormula(SalaryType type, Double baseAmount, long presentDays) {
        if (type == SalaryType.MONTHLY) {
            return "(" + baseAmount + " / " + AppConstants.DAYS_IN_PAYROLL_MONTH + ") × " + presentDays;
        }
        return baseAmount + " × " + presentDays;
    }
}

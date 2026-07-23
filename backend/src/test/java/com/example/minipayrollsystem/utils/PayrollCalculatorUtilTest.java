package com.example.minipayrollsystem.utils;

import com.example.minipayrollsystem.enums.SalaryType;
import com.example.minipayrollsystem.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayrollCalculatorUtilTest {

    @Test
    void testCalculateSalary_Monthly() {
        double result = PayrollCalculatorUtil.calculateSalary(SalaryType.MONTHLY, 60000.0, 20);
        assertEquals(40000.0, result);
    }

    @Test
    void testCalculateSalary_Daily() {
        double result = PayrollCalculatorUtil.calculateSalary(SalaryType.DAILY, 500.0, 15);
        assertEquals(7500.0, result);
    }

    @Test
    void testCalculateSalary_NullBaseAmount() {
        assertThrows(BadRequestException.class, () ->
                PayrollCalculatorUtil.calculateSalary(SalaryType.MONTHLY, null, 10)
        );
    }

    @Test
    void testBuildFormula_Monthly() {
        assertEquals("(60000.0 / 30) × 20", PayrollCalculatorUtil.buildFormula(SalaryType.MONTHLY, 60000.0, 20));
    }
}

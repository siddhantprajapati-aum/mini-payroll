package com.example.minipayrollsystem.utils;

import com.example.minipayrollsystem.enums.SalaryType;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkingDaysUtilTest {

    @Test
    void countWeekdays_july2026() {
        assertEquals(23, WorkingDaysUtil.countWeekdays(YearMonth.of(2026, 7)));
    }

    @Test
    void countExpectedDays_monthlyUsesWeekdays() {
        assertEquals(23, WorkingDaysUtil.countExpectedDays(SalaryType.MONTHLY, YearMonth.of(2026, 7)));
    }

    @Test
    void countExpectedDays_dailyUsesCalendarDays() {
        assertEquals(31, WorkingDaysUtil.countExpectedDays(SalaryType.DAILY, YearMonth.of(2026, 7)));
    }
}

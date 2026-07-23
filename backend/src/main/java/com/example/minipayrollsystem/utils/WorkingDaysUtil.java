package com.example.minipayrollsystem.utils;

import com.example.minipayrollsystem.enums.SalaryType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

public final class WorkingDaysUtil {

    private WorkingDaysUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static long countExpectedDays(SalaryType salaryType, YearMonth yearMonth) {
        if (salaryType == SalaryType.DAILY) {
            return yearMonth.lengthOfMonth();
        }
        return countWeekdays(yearMonth);
    }

    public static long countWeekdays(YearMonth yearMonth) {
        long weekdays = 0;
        LocalDate date = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        while (!date.isAfter(end)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                weekdays++;
            }
            date = date.plusDays(1);
        }
        return weekdays;
    }

    public static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}

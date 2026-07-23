package com.example.minipayrollsystem.constants;

public final class AppConstants {

    private AppConstants() {
        throw new IllegalStateException("Constants class");
    }

    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final int MAX_PAGE_SIZE = 100;

    public static final int DAYS_IN_PAYROLL_MONTH = 30;
}

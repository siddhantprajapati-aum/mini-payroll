package com.example.minipayrollsystem.dto;

import com.example.minipayrollsystem.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AttendanceRequestDTO {

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Attendance date cannot be in the future")
    private LocalDate date;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    private String remarks;
}
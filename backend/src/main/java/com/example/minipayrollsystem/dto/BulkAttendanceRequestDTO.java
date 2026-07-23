package com.example.minipayrollsystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkAttendanceRequestDTO {

    @NotNull(message = "Attendance records are required")
    @NotEmpty(message = "At least one attendance record is required")
    private List<@Valid AttendanceRequestDTO> records;
}

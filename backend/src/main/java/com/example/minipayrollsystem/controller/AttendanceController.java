package com.example.minipayrollsystem.controller;

import com.example.minipayrollsystem.constants.AppConstants;
import com.example.minipayrollsystem.dto.AttendanceRequestDTO;
import com.example.minipayrollsystem.dto.BulkAttendanceRequestDTO;
import com.example.minipayrollsystem.entity.Attendance;
import com.example.minipayrollsystem.service.AttendanceService;
import com.example.minipayrollsystem.utils.PaginationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/{employeeId}")
    public ResponseEntity<Attendance> markAttendance(
            @PathVariable Long employeeId,
            @Valid @RequestBody AttendanceRequestDTO dto) {
        return ResponseEntity.ok(attendanceService.markAttendance(employeeId, dto));
    }

    @PostMapping("/{employeeId}/bulk")
    public ResponseEntity<List<Attendance>> markBulkAttendance(
            @PathVariable Long employeeId,
            @Valid @RequestBody BulkAttendanceRequestDTO dto) {
        return ResponseEntity.ok(attendanceService.markBulkAttendance(employeeId, dto.getRecords()));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<Page<Attendance>> getAttendanceHistory(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceHistory(employeeId, PaginationUtil.buildPageable(page, size))
        );
    }
}

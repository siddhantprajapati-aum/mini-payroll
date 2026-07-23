package com.example.minipayrollsystem.controller;

import com.example.minipayrollsystem.constants.AppConstants;
import com.example.minipayrollsystem.dto.LeaveRequestDTO;
import com.example.minipayrollsystem.entity.Leave;
import com.example.minipayrollsystem.enums.LeaveStatus;
import com.example.minipayrollsystem.service.LeaveService;
import com.example.minipayrollsystem.utils.PaginationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/{employeeId}")
    public ResponseEntity<Leave> applyLeave(
            @PathVariable Long employeeId,
            @Valid @RequestBody LeaveRequestDTO dto) {
        return ResponseEntity.ok(leaveService.applyLeave(employeeId, dto));
    }

    @PutMapping("/{leaveId}/status")
    public ResponseEntity<Leave> updateLeaveStatus(
            @PathVariable Long leaveId,
            @RequestParam LeaveStatus status) {
        return ResponseEntity.ok(leaveService.updateLeaveStatus(leaveId, status));
    }

    @GetMapping
    public ResponseEntity<Page<Leave>> getAllLeaves(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(leaveService.getAllLeaves(PaginationUtil.buildPageable(page, size)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<Leave>> getLeavesByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(
                leaveService.getLeavesByEmployee(employeeId, PaginationUtil.buildPageable(page, size))
        );
    }
}

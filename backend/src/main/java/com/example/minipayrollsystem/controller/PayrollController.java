package com.example.minipayrollsystem.controller;

import com.example.minipayrollsystem.dto.PayrollResponseDTO;
import com.example.minipayrollsystem.service.PayrollService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@Validated
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping("/{employeeId}")
    public ResponseEntity<PayrollResponseDTO> getPayroll(
            @PathVariable Long employeeId,
            @RequestParam @Min(2000) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month) {
        return ResponseEntity.ok(payrollService.generatePayroll(employeeId, year, month));
    }
}

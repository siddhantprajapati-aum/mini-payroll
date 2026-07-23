package com.example.minipayrollsystem.controller;

import com.example.minipayrollsystem.constants.AppConstants;
import com.example.minipayrollsystem.dto.EmployeeRequestDTO;
import com.example.minipayrollsystem.entity.Employee;
import com.example.minipayrollsystem.service.EmployeeService;
import com.example.minipayrollsystem.utils.PaginationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
        return ResponseEntity.ok(employeeService.createEmployee(dto));
    }

    @GetMapping
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(employeeService.getAllEmployees(PaginationUtil.buildPageable(page, size)));
    }
}

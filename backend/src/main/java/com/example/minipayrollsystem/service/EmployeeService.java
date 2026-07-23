package com.example.minipayrollsystem.service;

import com.example.minipayrollsystem.dto.EmployeeRequestDTO;
import com.example.minipayrollsystem.entity.Employee;
import com.example.minipayrollsystem.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public Employee createEmployee(EmployeeRequestDTO dto) {
        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setSalaryType(dto.getSalaryType());
        employee.setSalaryAmount(dto.getSalaryAmount());
        return employeeRepository.save(employee);
    }

    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }
}
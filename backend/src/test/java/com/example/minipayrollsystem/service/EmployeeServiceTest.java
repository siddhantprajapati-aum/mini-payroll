package com.example.minipayrollsystem.service;

import com.example.minipayrollsystem.dto.EmployeeRequestDTO;
import com.example.minipayrollsystem.entity.Employee;
import com.example.minipayrollsystem.enums.Role;
import com.example.minipayrollsystem.enums.SalaryType;
import com.example.minipayrollsystem.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void testCreateEmployee_Success() {
        EmployeeRequestDTO request = new EmployeeRequestDTO();
        request.setName("John Doe");
        request.setRole(Role.OFFICE); // Adjust enum if your name is different
        request.setSalaryType(SalaryType.MONTHLY);
        request.setSalaryAmount(50000.0);

        Employee savedEmployee = new Employee();
        savedEmployee.setId(1L);
        savedEmployee.setName("John Doe");
        savedEmployee.setSalaryAmount(50000.0);

        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        Employee result = employeeService.createEmployee(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testGetAllEmployees_Paginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Employee emp = new Employee();
        emp.setName("Jane Doe");
        
        Page<Employee> mockPage = new PageImpl<>(List.of(emp));

        when(employeeRepository.findAll(pageable)).thenReturn(mockPage);

        Page<Employee> result = employeeService.getAllEmployees(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Jane Doe", result.getContent().get(0).getName());
        verify(employeeRepository, times(1)).findAll(pageable);
    }
}
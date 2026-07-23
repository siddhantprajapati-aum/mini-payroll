package com.example.minipayrollsystem.controller;

import com.example.minipayrollsystem.dto.EmployeeRequestDTO;
import com.example.minipayrollsystem.entity.Employee;
import com.example.minipayrollsystem.enums.Role;
import com.example.minipayrollsystem.enums.SalaryType;
import com.example.minipayrollsystem.exception.GlobalExceptionHandler;
import com.example.minipayrollsystem.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void testCreateEmployee_ValidInput_Returns200() throws Exception {
        EmployeeRequestDTO validRequest = new EmployeeRequestDTO();
        validRequest.setName("Alice Smith");
        validRequest.setRole(Role.OFFICE);
        validRequest.setSalaryType(SalaryType.MONTHLY);
        validRequest.setSalaryAmount(50000.0);

        Employee mockEmployee = new Employee();
        mockEmployee.setId(1L);
        mockEmployee.setName("Alice Smith");

        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(mockEmployee);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }

    @Test
    void testCreateEmployee_InvalidInput_Returns400BadRequest() throws Exception {
        EmployeeRequestDTO invalidRequest = new EmployeeRequestDTO();
        invalidRequest.setName("");
        invalidRequest.setRole(Role.OFFICE);
        invalidRequest.setSalaryType(SalaryType.MONTHLY);
        invalidRequest.setSalaryAmount(-1000.0);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllEmployees_ReturnsPaginatedList() throws Exception {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("Alice Smith");

        when(employeeService.getAllEmployees(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(employee)));

        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Alice Smith"));
    }
}

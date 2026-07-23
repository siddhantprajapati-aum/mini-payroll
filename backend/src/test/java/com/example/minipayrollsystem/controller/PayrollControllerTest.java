package com.example.minipayrollsystem.controller;

import com.example.minipayrollsystem.dto.PayrollResponseDTO;
import com.example.minipayrollsystem.exception.GlobalExceptionHandler;
import com.example.minipayrollsystem.service.PayrollService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayrollController.class)
@Import(GlobalExceptionHandler.class)
class PayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollService payrollService;

    @Test
    void testGetPayroll_Returns200AndDTO() throws Exception {
        PayrollResponseDTO mockResponse = PayrollResponseDTO.builder()
                .employeeId(1L)
                .employeeName("John Doe")
                .salaryType("MONTHLY")
                .baseSalaryAmount(60000.0)
                .year(2026)
                .month(7)
                .workingDays(23)
                .presentDays(20)
                .absentDays(2)
                .unmarkedDays(1)
                .calculatedSalary(40000.0)
                .formula("(60000.0 / 30) × 20")
                .build();

        when(payrollService.generatePayroll(1L, 2026, 7)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/payroll/1")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.calculatedSalary").value(40000.0))
                .andExpect(jsonPath("$.formula").value("(60000.0 / 30) × 20"));
    }

    @Test
    void testGetPayroll_InvalidMonth_Returns400() throws Exception {
        mockMvc.perform(get("/api/v1/payroll/1")
                        .param("year", "2026")
                        .param("month", "13"))
                .andExpect(status().isBadRequest());
    }
}

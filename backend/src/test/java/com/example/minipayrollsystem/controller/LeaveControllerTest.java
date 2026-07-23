package com.example.minipayrollsystem.controller;

import com.example.minipayrollsystem.dto.LeaveRequestDTO;
import com.example.minipayrollsystem.entity.Leave;
import com.example.minipayrollsystem.enums.LeaveStatus;
import com.example.minipayrollsystem.exception.GlobalExceptionHandler;
import com.example.minipayrollsystem.service.LeaveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaveController.class)
@Import(GlobalExceptionHandler.class)
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LeaveService leaveService;

    @Test
    void testApplyLeave_ValidInput_Returns200() throws Exception {
        LeaveRequestDTO request = new LeaveRequestDTO();
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReason("Vacation");

        Leave mockLeave = new Leave();
        mockLeave.setId(1L);
        mockLeave.setStatus(LeaveStatus.PENDING);

        when(leaveService.applyLeave(eq(1L), any(LeaveRequestDTO.class))).thenReturn(mockLeave);

        mockMvc.perform(post("/api/v1/leaves/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testUpdateLeaveStatus_Returns200() throws Exception {
        Leave mockLeave = new Leave();
        mockLeave.setId(1L);
        mockLeave.setStatus(LeaveStatus.APPROVED);

        when(leaveService.updateLeaveStatus(1L, LeaveStatus.APPROVED)).thenReturn(mockLeave);

        mockMvc.perform(put("/api/v1/leaves/1/status")
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void testGetAllLeaves_ReturnsPaginatedList() throws Exception {
        Page<Leave> mockPage = new PageImpl<>(List.of(new Leave()));

        when(leaveService.getAllLeaves(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/leaves")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetLeavesByEmployee_ReturnsPaginatedList() throws Exception {
        Page<Leave> mockPage = new PageImpl<>(List.of(new Leave()));

        when(leaveService.getLeavesByEmployee(eq(1L), any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/leaves/employee/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

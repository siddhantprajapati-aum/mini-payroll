package com.example.minipayrollsystem.controller;

import com.example.minipayrollsystem.dto.AttendanceRequestDTO;
import com.example.minipayrollsystem.dto.BulkAttendanceRequestDTO;
import com.example.minipayrollsystem.entity.Attendance;
import com.example.minipayrollsystem.enums.AttendanceStatus;
import com.example.minipayrollsystem.exception.GlobalExceptionHandler;
import com.example.minipayrollsystem.service.AttendanceService;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
@Import(GlobalExceptionHandler.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AttendanceService attendanceService;

    @Test
    void testMarkAttendance_ValidInput_Returns200() throws Exception {
        AttendanceRequestDTO request = new AttendanceRequestDTO();
        request.setDate(LocalDate.now());
        request.setStatus(AttendanceStatus.PRESENT);

        Attendance mockAttendance = new Attendance();
        mockAttendance.setId(1L);
        mockAttendance.setStatus(AttendanceStatus.PRESENT);

        when(attendanceService.markAttendance(eq(1L), any(AttendanceRequestDTO.class))).thenReturn(mockAttendance);

        mockMvc.perform(post("/api/v1/attendances/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PRESENT"));
    }

    @Test
    void testMarkAttendance_FutureDate_Returns400() throws Exception {
        AttendanceRequestDTO request = new AttendanceRequestDTO();
        request.setDate(LocalDate.now().plusDays(5));
        request.setStatus(AttendanceStatus.PRESENT);

        mockMvc.perform(post("/api/v1/attendances/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMarkBulkAttendance_Returns200() throws Exception {
        AttendanceRequestDTO record = new AttendanceRequestDTO();
        record.setDate(LocalDate.now());
        record.setStatus(AttendanceStatus.PRESENT);

        BulkAttendanceRequestDTO request = new BulkAttendanceRequestDTO();
        request.setRecords(List.of(record));

        Attendance mockAttendance = new Attendance();
        mockAttendance.setId(1L);
        mockAttendance.setStatus(AttendanceStatus.PRESENT);

        when(attendanceService.markBulkAttendance(eq(1L), anyList())).thenReturn(List.of(mockAttendance));

        mockMvc.perform(post("/api/v1/attendances/1/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testGetAttendanceHistory_ReturnsPaginatedList() throws Exception {
        Page<Attendance> mockPage = new PageImpl<>(List.of(new Attendance()));

        when(attendanceService.getAttendanceHistory(eq(1L), any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/attendances/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

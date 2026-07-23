package com.example.minipayrollsystem.service;

import com.example.minipayrollsystem.dto.AttendanceRequestDTO;
import com.example.minipayrollsystem.entity.Attendance;
import com.example.minipayrollsystem.entity.Employee;
import com.example.minipayrollsystem.enums.AttendanceStatus;
import com.example.minipayrollsystem.exception.ResourceNotFoundException;
import com.example.minipayrollsystem.repository.AttendanceRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void testMarkAttendance_Success() {
        Long employeeId = 1L;

        AttendanceRequestDTO request = new AttendanceRequestDTO();
        request.setDate(LocalDate.now());
        request.setStatus(AttendanceStatus.PRESENT);
        request.setRemarks("On time");

        Employee mockEmployee = new Employee();
        mockEmployee.setId(employeeId);
        mockEmployee.setName("John Doe");

        Attendance savedAttendance = new Attendance();
        savedAttendance.setId(100L);
        savedAttendance.setEmployee(mockEmployee);
        savedAttendance.setDate(request.getDate());
        savedAttendance.setStatus(request.getStatus());
        savedAttendance.setRemarks(request.getRemarks());

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, request.getDate()))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(savedAttendance);

        Attendance result = attendanceService.markAttendance(employeeId, request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(AttendanceStatus.PRESENT, result.getStatus());
        assertEquals("John Doe", result.getEmployee().getName());

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testMarkAttendance_UpsertsExistingRecord() {
        Long employeeId = 1L;
        LocalDate date = LocalDate.now();

        AttendanceRequestDTO request = new AttendanceRequestDTO();
        request.setDate(date);
        request.setStatus(AttendanceStatus.ABSENT);
        request.setRemarks("Updated");

        Employee mockEmployee = new Employee();
        mockEmployee.setId(employeeId);

        Attendance existing = new Attendance();
        existing.setId(50L);
        existing.setEmployee(mockEmployee);
        existing.setDate(date);
        existing.setStatus(AttendanceStatus.PRESENT);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, date)).thenReturn(Optional.of(existing));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attendance result = attendanceService.markAttendance(employeeId, request);

        assertEquals(50L, result.getId());
        assertEquals(AttendanceStatus.ABSENT, result.getStatus());
        assertEquals("Updated", result.getRemarks());
    }

    @Test
    void testMarkAttendance_EmployeeNotFound() {
        Long employeeId = 99L;
        AttendanceRequestDTO request = new AttendanceRequestDTO();
        request.setDate(LocalDate.now());
        request.setStatus(AttendanceStatus.PRESENT);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                attendanceService.markAttendance(employeeId, request)
        );

        assertEquals("Employee not found", exception.getMessage());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testMarkBulkAttendance_Success() {
        Long employeeId = 1L;
        Employee mockEmployee = new Employee();
        mockEmployee.setId(employeeId);

        AttendanceRequestDTO first = new AttendanceRequestDTO();
        first.setDate(LocalDate.now().minusDays(1));
        first.setStatus(AttendanceStatus.PRESENT);

        AttendanceRequestDTO second = new AttendanceRequestDTO();
        second.setDate(LocalDate.now());
        second.setStatus(AttendanceStatus.ABSENT);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(attendanceRepository.findByEmployeeIdAndDate(eq(employeeId), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Attendance> result = attendanceService.markBulkAttendance(employeeId, List.of(first, second));

        assertEquals(2, result.size());
        verify(attendanceRepository, times(2)).save(any(Attendance.class));
    }

    @Test
    void testGetAttendanceHistory_Paginated() {
        Long employeeId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Employee mockEmployee = new Employee();
        mockEmployee.setId(employeeId);

        Attendance attendance = new Attendance();
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setDate(LocalDate.now());

        Page<Attendance> mockPage = new PageImpl<>(List.of(attendance));

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(attendanceRepository.findByEmployeeId(employeeId, pageable)).thenReturn(mockPage);

        Page<Attendance> result = attendanceService.getAttendanceHistory(employeeId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(AttendanceStatus.PRESENT, result.getContent().get(0).getStatus());
    }
}

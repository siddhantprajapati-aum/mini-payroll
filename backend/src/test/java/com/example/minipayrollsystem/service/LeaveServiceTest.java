package com.example.minipayrollsystem.service;

import com.example.minipayrollsystem.dto.LeaveRequestDTO;
import com.example.minipayrollsystem.entity.Employee;
import com.example.minipayrollsystem.entity.Leave;
import com.example.minipayrollsystem.enums.LeaveStatus;
import com.example.minipayrollsystem.exception.BadRequestException;
import com.example.minipayrollsystem.exception.ResourceNotFoundException;
import com.example.minipayrollsystem.repository.EmployeeRepository;
import com.example.minipayrollsystem.repository.LeaveRepository;
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
class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    @Test
    void testApplyLeave_Success() {
        Long employeeId = 1L;
        Employee employee = new Employee();
        employee.setId(employeeId);

        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(3));
        dto.setReason("Vacation");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(leaveRepository.save(any(Leave.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Leave result = leaveService.applyLeave(employeeId, dto);

        assertEquals(LeaveStatus.PENDING, result.getStatus());
        assertEquals("Vacation", result.getReason());
        assertEquals(employee, result.getEmployee());
    }

    @Test
    void testApplyLeave_EndDateBeforeStartDate() {
        Long employeeId = 1L;
        Employee employee = new Employee();
        employee.setId(employeeId);

        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now().plusDays(1));
        dto.setReason("Invalid");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                leaveService.applyLeave(employeeId, dto)
        );

        assertEquals("End date cannot be before start date", exception.getMessage());
        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void testUpdateLeaveStatus_Approve() {
        Long leaveId = 1L;
        Leave existingLeave = new Leave();
        existingLeave.setId(leaveId);
        existingLeave.setStatus(LeaveStatus.PENDING);

        when(leaveRepository.findById(leaveId)).thenReturn(Optional.of(existingLeave));
        when(leaveRepository.save(any(Leave.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Leave updatedLeave = leaveService.updateLeaveStatus(leaveId, LeaveStatus.APPROVED);

        assertEquals(LeaveStatus.APPROVED, updatedLeave.getStatus());
        assertNotNull(updatedLeave.getReviewedAt());
        verify(leaveRepository, times(1)).save(existingLeave);
    }

    @Test
    void testUpdateLeaveStatus_AlreadyReviewed() {
        Long leaveId = 1L;
        Leave existingLeave = new Leave();
        existingLeave.setId(leaveId);
        existingLeave.setStatus(LeaveStatus.APPROVED);

        when(leaveRepository.findById(leaveId)).thenReturn(Optional.of(existingLeave));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                leaveService.updateLeaveStatus(leaveId, LeaveStatus.REJECTED)
        );

        assertEquals("Only PENDING leaves can be reviewed", exception.getMessage());
        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void testUpdateLeaveStatus_InvalidTargetStatus() {
        Long leaveId = 1L;
        Leave existingLeave = new Leave();
        existingLeave.setId(leaveId);
        existingLeave.setStatus(LeaveStatus.PENDING);

        when(leaveRepository.findById(leaveId)).thenReturn(Optional.of(existingLeave));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                leaveService.updateLeaveStatus(leaveId, LeaveStatus.PENDING)
        );

        assertEquals("Status must be APPROVED or REJECTED", exception.getMessage());
    }

    @Test
    void testGetAllLeaves_Paginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Leave leave = new Leave();
        leave.setReason("Sick Leave");

        Page<Leave> mockPage = new PageImpl<>(List.of(leave));
        when(leaveRepository.findAll(pageable)).thenReturn(mockPage);

        Page<Leave> result = leaveService.getAllLeaves(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Sick Leave", result.getContent().get(0).getReason());
    }

    @Test
    void testGetLeavesByEmployee_Success() {
        Long employeeId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Leave leave = new Leave();
        leave.setReason("Personal");

        when(employeeRepository.existsById(employeeId)).thenReturn(true);
        when(leaveRepository.findByEmployeeId(employeeId, pageable))
                .thenReturn(new PageImpl<>(List.of(leave)));

        Page<Leave> result = leaveService.getLeavesByEmployee(employeeId, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetLeavesByEmployee_NotFound() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                leaveService.getLeavesByEmployee(99L, PageRequest.of(0, 10))
        );
    }
}

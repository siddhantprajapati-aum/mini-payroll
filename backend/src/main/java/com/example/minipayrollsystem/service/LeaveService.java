package com.example.minipayrollsystem.service;

import com.example.minipayrollsystem.dto.LeaveRequestDTO;
import com.example.minipayrollsystem.entity.Employee;
import com.example.minipayrollsystem.entity.Leave;
import com.example.minipayrollsystem.enums.LeaveStatus;
import com.example.minipayrollsystem.exception.BadRequestException;
import com.example.minipayrollsystem.exception.ResourceNotFoundException;
import com.example.minipayrollsystem.repository.EmployeeRepository;
import com.example.minipayrollsystem.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Leave applyLeave(Long employeeId, LeaveRequestDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setReason(dto.getReason());
        leave.setStatus(LeaveStatus.PENDING);

        return leaveRepository.save(leave);
    }

    @Transactional
    public Leave updateLeaveStatus(Long leaveId, LeaveStatus status) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only PENDING leaves can be reviewed");
        }
        if (status != LeaveStatus.APPROVED && status != LeaveStatus.REJECTED) {
            throw new BadRequestException("Status must be APPROVED or REJECTED");
        }

        leave.setStatus(status);
        leave.setReviewedAt(LocalDateTime.now());
        return leaveRepository.save(leave);
    }

    public Page<Leave> getAllLeaves(Pageable pageable) {
        return leaveRepository.findAll(pageable);
    }

    public Page<Leave> getLeavesByEmployee(Long employeeId, Pageable pageable) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found");
        }
        return leaveRepository.findByEmployeeId(employeeId, pageable);
    }
}

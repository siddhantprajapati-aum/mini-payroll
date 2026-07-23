package com.example.minipayrollsystem.service;

import com.example.minipayrollsystem.dto.AttendanceRequestDTO;
import com.example.minipayrollsystem.entity.Attendance;
import com.example.minipayrollsystem.entity.Employee;
import com.example.minipayrollsystem.exception.ResourceNotFoundException;
import com.example.minipayrollsystem.repository.AttendanceRepository;
import com.example.minipayrollsystem.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Attendance markAttendance(Long employeeId, AttendanceRequestDTO dto) {
        Employee employee = findEmployeeOrThrow(employeeId);
        return upsertAttendance(employee, dto);
    }

    @Transactional
    public List<Attendance> markBulkAttendance(Long employeeId, List<AttendanceRequestDTO> records) {
        Employee employee = findEmployeeOrThrow(employeeId);
        List<Attendance> saved = new ArrayList<>();
        for (AttendanceRequestDTO record : records) {
            saved.add(upsertAttendance(employee, record));
        }
        return saved;
    }

    public Page<Attendance> getAttendanceHistory(Long employeeId, Pageable pageable) {
        findEmployeeOrThrow(employeeId);
        return attendanceRepository.findByEmployeeId(employeeId, pageable);
    }

    private Attendance upsertAttendance(Employee employee, AttendanceRequestDTO dto) {
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employee.getId(), dto.getDate())
                .orElseGet(Attendance::new);

        attendance.setEmployee(employee);
        attendance.setDate(dto.getDate());
        attendance.setStatus(dto.getStatus());
        attendance.setRemarks(dto.getRemarks());

        return attendanceRepository.save(attendance);
    }

    private Employee findEmployeeOrThrow(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }
}

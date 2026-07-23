package com.example.minipayrollsystem.repository;

import com.example.minipayrollsystem.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate start, LocalDate end);

    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);

    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
}

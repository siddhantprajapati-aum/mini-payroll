package com.example.minipayrollsystem.repository;

import com.example.minipayrollsystem.entity.Leave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    Page<Leave> findByEmployeeId(Long employeeId, Pageable pageable);
}
package org.example.kpitelegrambot.postgresql.entity.repository;

import jakarta.transaction.Transactional;
import org.example.kpitelegrambot.postgresql.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Transactional
    @Modifying
    @Query("UPDATE Employee e SET e.fired = :firedDate WHERE e.username = :username")
    void dismissEmployeeByUsername(@Param("username") String username,
                                   @Param("firedDate") LocalDate firedDate);
}

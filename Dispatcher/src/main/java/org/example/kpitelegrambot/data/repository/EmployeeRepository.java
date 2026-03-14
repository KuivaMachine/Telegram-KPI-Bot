package org.example.kpitelegrambot.data.repository;

import jakarta.transaction.Transactional;
import org.example.kpitelegrambot.data.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Transactional
    @Modifying
    @Query("UPDATE Employee e SET e.fired = :firedDate WHERE e.username = :username")
    void dismissEmployeeByUsername(@Param("username") String username,
                                   @Param("firedDate") LocalDate firedDate);

    @Query(value = "SELECT * FROM employees WHERE fired IS NULL AND fio IS NOT NULL", nativeQuery = true)
    List<Employee> getUnfiredEmployees();
}

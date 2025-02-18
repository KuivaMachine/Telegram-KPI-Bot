package org.example.kpitelegrambot.postgresql.entity.repository;

import org.example.kpitelegrambot.postgresql.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}

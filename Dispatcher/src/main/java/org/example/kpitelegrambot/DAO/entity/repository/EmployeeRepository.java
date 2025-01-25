package org.example.kpitelegrambot.DAO.entity.repository;

import org.example.kpitelegrambot.DAO.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}

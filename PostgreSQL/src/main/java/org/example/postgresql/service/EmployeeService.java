package org.example.postgresql.service;

import org.example.postgresql.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public interface EmployeeService {
    void save(Employee employee);
    Employee getEmployeeByChatId(long id);
    void deleteEmployeeByChatId(long id);


}

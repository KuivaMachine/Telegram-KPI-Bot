package org.example.kpitelegrambot.service;

import org.example.kpitelegrambot.DAO.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public interface EmployeeService {
    void save(Employee employee);
    Employee getEmployeeByChatId(long id);
    void deleteEmployeeByChatId(long id);


}

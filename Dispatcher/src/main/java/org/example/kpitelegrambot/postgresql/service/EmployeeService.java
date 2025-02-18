package org.example.kpitelegrambot.postgresql.service;

import org.example.kpitelegrambot.postgresql.entity.Employee;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface EmployeeService {
    void save(Employee employee);
    Employee getEmployeeByChatId(long id);
    void deleteEmployeeByChatId(long id);
    List<Employee> getListOfDayPrinters();
    List<Employee> getListOfNightPrinters();
}

package org.example.kpitelegrambot.data.service;

import org.example.kpitelegrambot.bot.enums.DayNight;
import org.example.kpitelegrambot.data.entity.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeService {
    void save(Employee employee);
    Employee getEmployeeByChatId(long id);
    void deleteEmployeeByChatId(long id);
    List<Employee> getListOfPrinters(DayNight mode);
    List<Employee> getEmployees();
    void dismissEmployeeByUsername(String username, LocalDate date);
}

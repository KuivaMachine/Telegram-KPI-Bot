package org.example.kpitelegrambot.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.kpitelegrambot.data.DayNight;
import org.example.kpitelegrambot.data.EmployeePost;
import org.example.kpitelegrambot.data.EmployeeStatus;
import org.example.kpitelegrambot.entity.Employee;
import org.example.kpitelegrambot.entity.repository.EmployeeRepository;
import org.example.kpitelegrambot.service.EmployeeService;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeServiceImpl implements EmployeeService {

    EmployeeRepository employeeRepository;
    @Override
    public void save(Employee employee) {
        employeeRepository.save(employee);
    }

    @Override
    public Employee getEmployeeByChatId(long id) {
        return employeeRepository.findById(id).orElse(new Employee(null,null,null, EmployeeStatus.UNKNOWN_USER, EmployeePost.UNKNOWN, DayNight.DAY));
    }

    @Override
    public void deleteEmployeeByChatId(long id) {
        employeeRepository.deleteById(id);
    }



}

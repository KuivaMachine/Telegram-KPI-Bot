package org.example.postgresql.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.postgresql.data.DayNight;
import org.example.postgresql.data.EmployeePost;
import org.example.postgresql.data.EmployeeStatus;
import org.example.postgresql.entity.Employee;
import org.example.postgresql.entity.repository.EmployeeRepository;
import org.example.postgresql.service.EmployeeService;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public void save(Employee employee) {
        employeeRepository.save(employee);
    }

    @Override
    public Employee getEmployeeByChatId(long id) {
        return employeeRepository.findById(id).orElse(new Employee(id,null,null, EmployeeStatus.UNKNOWN_USER, EmployeePost.UNKNOWN, DayNight.UNKNOWN));
    }

    @Override
    public void deleteEmployeeByChatId(long id) {
        employeeRepository.deleteById(id);
    }



}

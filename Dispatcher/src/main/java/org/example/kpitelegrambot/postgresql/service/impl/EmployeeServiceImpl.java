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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    EmployeeRepository employeeRepository;

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

    @Override
    public List<Employee> getListOfDayPrinters() {
        List<Employee> dayEmployees = new ArrayList<>();
        for (Employee employee : employeeRepository.findAll()) {
                if(employee.getWorkTime()==DayNight.DAY){
                    dayEmployees.add(employee);
            }
        }
        return dayEmployees;
    }

    @Override
    public List<Employee> getListOfNightPrinters() {
        List<Employee> nightEmployees = new ArrayList<>();
        for (Employee employee : employeeRepository.findAll()) {
            if(employee.getWorkTime()==DayNight.NIGHT){
                nightEmployees.add(employee);
            }
        }
        return nightEmployees;
    }
}

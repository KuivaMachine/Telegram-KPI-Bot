package org.example.kpitelegrambot.data.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.kpitelegrambot.bot.enums.DayNight;
import org.example.kpitelegrambot.bot.enums.EmployeePost;
import org.example.kpitelegrambot.bot.enums.EmployeeStatus;
import org.example.kpitelegrambot.data.entity.Employee;
import org.example.kpitelegrambot.data.repository.EmployeeRepository;
import org.example.kpitelegrambot.data.service.EmployeeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


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
        return employeeRepository.findById(id).orElse(new Employee(id, null, null, EmployeeStatus.UNKNOWN_USER, EmployeePost.UNKNOWN, DayNight.UNKNOWN, null));
    }

    @Override
    public void deleteEmployeeByChatId(long id) {
        employeeRepository.deleteById(id);
    }

    @Override
    public List<Employee> getListOfPrinters(DayNight mode) {
        List<Employee> employees = new ArrayList<>();
        for (Employee employee : employeeRepository.findAll()) {
            if (employee.getWorkTime() == mode && checkFired(employee.getFired())) {
                employees.add(employee);
            }
        }
        return employees;
    }

    @Override
    public List<Employee> getEmployees() {
        return employeeRepository.getUnfiredEmployees();
    }

    @Override
    public void dismissEmployeeByUsername(String employeeName, LocalDate date) {
        employeeRepository.dismissEmployeeByUsername(employeeName, date);
    }

    /**
     * Сравнивает дату увольнения с текущей датой.
     *
     * @param fired дата увольнения
     * @return true, если даты увольнения нет, или она входит в месяц этого года.
     */
    private boolean checkFired(LocalDate fired) {
        if (fired == null) {
            return true;
        }
        LocalDate today = LocalDate.now();
        return fired.getMonth() == today.getMonth()
                && fired.getYear() == today.getYear();
    }
}

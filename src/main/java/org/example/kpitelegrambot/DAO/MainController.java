/*
package org.example.kpitelegrambot.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.example.kpitelegrambot.entity.Employee;
import org.example.kpitelegrambot.entity.repository.EmployeeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MainController {

    EmployeeRepository employeeRepository;
    ObjectMapper objectMapper;

    @PostMapping("/kpi/add/addUser")
    public void addUser(@RequestBody Employee employee) {
        employeeRepository.save(employee);
      
    }

    @SneakyThrows
    @GetMapping("/kpi/get/getAllUsers")
    public String getAllUsers() {
        List<Employee> employees = employeeRepository.findAll();
        return objectMapper.writeValueAsString(employees);
    }

    @SneakyThrows
    @GetMapping("/kpi/get/getById")
    public String getUserById(@RequestParam int id) {
        return objectMapper.writeValueAsString(employeeRepository.findById(id).orElseThrow());
    }

    @DeleteMapping("/kpi/delete/deleteUserByID")
    public void deleteUser(@RequestParam int id) {
        employeeRepository.deleteById(id);
    }
}
*/

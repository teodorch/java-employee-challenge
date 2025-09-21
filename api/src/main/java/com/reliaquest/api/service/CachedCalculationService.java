package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.client.api.DeleteEmployeeRequest;
import com.reliaquest.api.client.api.Response;
import com.reliaquest.api.controller.api.CreateEmployeeRequest;
import com.reliaquest.api.logging.Audited;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.exception.EmployeeNotFoundException;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service
@AllArgsConstructor
@Validated
@Audited
public class CachedCalculationService {

    private static final Map<String, Employee> EMPLOYEES = new HashMap<>();
    private static final TreeSet<Employee> TOP_EMPLOYEES =
            new TreeSet<>(Comparator.comparingInt(Employee::employeeSalary).reversed());

    @Getter
    private static final Map<String, List<Employee>> SEARCH_CACHE = new HashMap<>();
    private static final int MAX_CACHE_SIZE = 1000;
    private static Integer HIGHEST_SALARY = -1;


    @Autowired
    private final EmployeeClient employeeClient;


    @CacheEvict(value = "employees", allEntries = true)
    public List<Employee> getAll() {
        if (EMPLOYEES.isEmpty()) {
            Map<String, Employee> data = employeeClient.getAll().data()
                    .stream()
                    .collect(toMap(employee -> employee.id().toString(), Function.identity()));
            EMPLOYEES.putAll(data);
        }

        return new ArrayList<>(EMPLOYEES.values());
    }

    @Cacheable(value = "employees", key = "#id")
    public Optional<Employee> getById(@Valid @UUID String id) {
        if (EMPLOYEES.containsKey(id)) {
            return Optional.ofNullable(EMPLOYEES.get(id));
        } else {
            try {
                Employee employee = employeeClient.getById(id)
                        .data();
                return Optional.of(employee);
            } catch (FeignException.FeignClientException.NotFound e) {
                return Optional.empty();
            }
        }
    }

    public List<Employee> getByName(String fragment) {
        String searchTerm = fragment.toLowerCase();

        if (SEARCH_CACHE.containsKey(searchTerm)) {
            return SEARCH_CACHE.get(searchTerm);
        }

        if (EMPLOYEES.isEmpty()) {
            getAll();
        }

        List<Employee> results = EMPLOYEES.values()
                .stream()
                .filter(emp -> emp.employeeName().toLowerCase().contains(searchTerm))
                .toList();

        if (SEARCH_CACHE.size() >= MAX_CACHE_SIZE) {
            SEARCH_CACHE.clear();
        }

        SEARCH_CACHE.put(searchTerm, results);

        return results;
    }

    public List<String> getTopTenEmployees() {
        if (EMPLOYEES.isEmpty()) {
            getAll();
        }

        if (TOP_EMPLOYEES.size() < 10) {
            for (Employee emp : EMPLOYEES.values()) {
                TOP_EMPLOYEES.add(emp);
                keepTenTopEmployees();
            }
        }

        return TOP_EMPLOYEES.stream()
                .map(Employee::employeeName)
                .toList();
    }

    public Integer getHighestSalary() {
        if (HIGHEST_SALARY != -1) {
            return HIGHEST_SALARY;
        }

        if (TOP_EMPLOYEES.size() < 10) {
            getTopTenEmployees();
        }

        Integer employeeSalary = TOP_EMPLOYEES.first().employeeSalary();
        HIGHEST_SALARY = employeeSalary;

        return employeeSalary;
    }


    public Employee create(CreateEmployeeRequest createEmployeeRequest) {
        Employee newEmployee = employeeClient.create(createEmployeeRequest).data();

        EMPLOYEES.put(newEmployee.id().toString(), newEmployee);

        if (!TOP_EMPLOYEES.isEmpty()) {
            Employee lastEmployee = TOP_EMPLOYEES.last();
            Integer lastSalary = lastEmployee.employeeSalary();
            if (newEmployee.employeeSalary() > lastSalary) {
                TOP_EMPLOYEES.add(newEmployee);
                keepTenTopEmployees();
            }
        }

        if (newEmployee.employeeSalary() > HIGHEST_SALARY) {
            HIGHEST_SALARY = newEmployee.employeeSalary();
        }

        SEARCH_CACHE.clear();

        return newEmployee;
    }


    @CacheEvict(value = "employees", allEntries = true)
    public String delete(String id) {
        Employee employee = getById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));


        String name = employee.employeeName();
        DeleteEmployeeRequest deleteEmployeeRequest = new DeleteEmployeeRequest(name);
        Response<Boolean> response = employeeClient.delete(deleteEmployeeRequest);
        Boolean delete = response.data();

        if (!delete) {
            throw new RuntimeException(response.status());
        }

        EMPLOYEES.remove(id);
        TOP_EMPLOYEES.remove(employee);
        SEARCH_CACHE.clear();

        if (Objects.equals(HIGHEST_SALARY, employee.employeeSalary())) {
            HIGHEST_SALARY = -1;
        }

        return name;
    }

    private static void keepTenTopEmployees() {
        if (TOP_EMPLOYEES.size() > 10) {
            TOP_EMPLOYEES.pollLast();
        }
    }

    public static void invalidateCache() {
        EMPLOYEES.clear();
        TOP_EMPLOYEES.clear();
        SEARCH_CACHE.clear();
        HIGHEST_SALARY = -1;
    }
}

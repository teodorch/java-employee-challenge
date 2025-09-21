package com.reliaquest.api.controller;

import com.reliaquest.api.controller.api.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.CachedCalculationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
@AllArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeRequest> {

    private final CachedCalculationService employeeService;

    @Override
    @GetMapping("/all")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAll());
    }

    @Override
    @GetMapping("/get-by-name")
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@RequestParam String name) {
        return ResponseEntity.ok(employeeService.getByName(name));
    }

    @Override
    @GetMapping("/get-by-id")
    public ResponseEntity<Employee> getEmployeeById(@RequestParam String id) {
        return employeeService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping("/highest-salary")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return ResponseEntity.ok(employeeService.getHighestSalary());
    }

    @Override
    @GetMapping("/top-ten-highest-earning")
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return ResponseEntity.ok(employeeService.getTopTenEmployees());
    }

    @Override
    @PostMapping("/create")
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody CreateEmployeeRequest employeeInput) {
        return ResponseEntity.ok(employeeService.create(employeeInput));
    }

    @Override
    @DeleteMapping("/delete-by-id")
    public ResponseEntity<String> deleteEmployeeById(@RequestParam String id) {
        return ResponseEntity.ok(employeeService.delete(id));
    }
}

package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

public record Employee(UUID id, @JsonAlias("employee_name") String employeeName,
                       @JsonAlias("employee_salary") Integer employeeSalary,
                       @JsonAlias("employee_age") Integer employeeAge,
                       @JsonAlias("employee_title") String employeeTitle,
                       @JsonAlias("employee_email") String employeeEmail) {
}

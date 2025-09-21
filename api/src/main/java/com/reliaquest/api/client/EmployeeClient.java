package com.reliaquest.api.client;

import com.reliaquest.api.client.api.DeleteEmployeeRequest;
import com.reliaquest.api.client.api.Response;
import com.reliaquest.api.controller.api.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "employee-client", url = "${employee.url}")
public interface EmployeeClient {

    @GetMapping
    Response<List<Employee>> getAll();

    @GetMapping("/{id}")
    Response<Employee> getById(@PathVariable String id);

    @PostMapping
    @Retry(name = "remoteServiceRetry", fallbackMethod = "fallback")
    Response<Employee> create(@RequestBody CreateEmployeeRequest createEmployeeRequest);

    @DeleteMapping
    Response<Boolean> delete(@RequestBody DeleteEmployeeRequest name);

    default Response fallback(CreateEmployeeRequest o, Throwable t) {
        return new Response<>(null, "Temporarily unavailable due to rate limiting. Please try again later.");
    }
}

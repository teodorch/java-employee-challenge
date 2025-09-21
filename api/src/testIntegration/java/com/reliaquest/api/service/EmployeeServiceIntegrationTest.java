package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.client.api.DeleteEmployeeRequest;
import com.reliaquest.api.client.api.Response;
import com.reliaquest.api.model.Employee;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class EmployeeServiceIntegrationTest {

    private static final String EMPLOYEE_ID = "f65040c9-7690-400f-9b19-3cab9c1497df";
    private static final String EMPLOYEE_NAME = "Billy Bob";
    private static final Employee EMPLOYEE = new Employee(
            UUID.fromString(EMPLOYEE_ID), EMPLOYEE_NAME, 1000, 25,
            "Documentation Engineer", "billy.bob@company.com");

    private static final String EMPLOYEE_ID_2 = "e314c74d-e044-4985-80cb-b8222b11f239";
    private static final String EMPLOYEE_NAME_2 = "Áki Ármannsson";
    private static final Employee EMPLOYEE_2 = new Employee(
            UUID.fromString(EMPLOYEE_ID_2),
            EMPLOYEE_NAME_2,
            2000,
            28,
            "Software Engineer",
            "aki.armannsson@company.com");

    private static final String STATUS = "Successfully processed request.";


    @MockBean
    private EmployeeClient employeeClient;

    @Autowired
    private CachedCalculationService employeeService;

    @BeforeEach
    void setUp() {
        CachedCalculationService.invalidateCache();
    }

    @Test
    void getById_calledTwice_callsExternalOnce() {
        when(employeeClient.getById(EMPLOYEE_ID)).thenReturn(new Response<>(EMPLOYEE, STATUS));

        employeeService.getById(EMPLOYEE_ID);
        employeeService.getById(EMPLOYEE_ID);

        verify(employeeClient).getById(EMPLOYEE_ID);
    }

    @Test
    void delete_givenEmployeeId_clearsEmployeeIdCache() {
        when(employeeClient.getById(EMPLOYEE_ID)).thenReturn(new Response<>(EMPLOYEE, STATUS));
        DeleteEmployeeRequest deleteEmployeeRequest = new DeleteEmployeeRequest(EMPLOYEE_NAME);
        when(employeeClient.delete(deleteEmployeeRequest)).thenReturn(new Response<>(true, STATUS));

        employeeService.getById(EMPLOYEE_ID);
        employeeService.getById(EMPLOYEE_ID);

        employeeService.delete(EMPLOYEE_ID);

        employeeService.getById(EMPLOYEE_ID);

        verify(employeeClient, times(2)).getById(EMPLOYEE_ID);
    }

    @Test
    void getById_givenNonUUID_throwsException() {
        assertThrows(ConstraintViolationException.class, () -> employeeService.getById("some-id"));
    }
}
package com.reliaquest.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.controller.api.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.CachedCalculationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private static final String EMPLOYEE_ID = "f65040c9-7690-400f-9b19-3cab9c1497df";
    private static final String EMPLOYEE_NAME = "Billy Bob";
    private static final Employee EMPLOYEE = new Employee(
            UUID.fromString(EMPLOYEE_ID), EMPLOYEE_NAME, 1000, 25, "Documentation Engineer", "billy.bob@company.com");
    private static final String EMPLOYEE_ID_2 = "e314c74d-e044-4985-80cb-b8222b11f239";
    private static final Employee EMPLOYEE_2 = new Employee(
            UUID.fromString(EMPLOYEE_ID_2),
            "Áki Ármannsson",
            2000,
            28,
            "Software Engineer",
            "aki.armannsson@company.com");

    @MockBean
    private CachedCalculationService employeeService;

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getAllEmployees_shouldReturnAllEmployees() throws Exception {
        when(employeeService.getAll()).thenReturn(asList(EMPLOYEE, EMPLOYEE_2));

        String response = getResponse(get("/employee/all"));

        assertEquals(asList(EMPLOYEE, EMPLOYEE_2),
                objectMapper.readValue(response, new TypeReference<List<Employee>>() {}));
    }

    @Test
    void getEmployeesByNameSearch_givenName_returnsCorrectEmployees() throws Exception {
        when(employeeService.getByName(EMPLOYEE_NAME)).thenReturn(singletonList(EMPLOYEE));

        String response = getResponse(get("/employee/get-by-name")
                .param("name", EMPLOYEE_NAME));

        assertEquals(singletonList(EMPLOYEE), objectMapper.readValue(response, new TypeReference<List<Employee>>() {}));
    }

    @Test
    void getEmployeeById_givenEmployeeId_returnsCorrectEmployee() throws Exception {
        when(employeeService.getById(EMPLOYEE_ID)).thenReturn(Optional.of(EMPLOYEE));

        String response = getResponse(get("/employee/get-by-id").param("id", EMPLOYEE_ID));

        assertEquals(EMPLOYEE, objectMapper.readValue(response, Employee.class));
    }

    @Test
    void getEmployeeById_givenNonExistingEmployeeId_returns404() throws Exception {
        when(employeeService.getById(EMPLOYEE_ID)).thenReturn(Optional.empty());

        mvc.perform(get("/employee/get-by-id").param("id", EMPLOYEE_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHighestSalaryOfEmployees_givenGet_returnsHighestSalary() throws Exception {
        when(employeeService.getHighestSalary()).thenReturn(2000);

        String response = getResponse(get("/employee/highest-salary"));

        assertEquals(2000, objectMapper.readValue(response, Integer.class));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_givenGet_returnsTopTenEarningEmployees() throws Exception {
        List<String> highest = asList(
                EMPLOYEE_NAME,
                "Áki Ármannsson",
                "John Doe",
                "Jane Doe",
                "John Smith",
                "Jane Smith",
                "John Johnson",
                "Jane Johnson",
                "John Brown",
                "Jane Brown");
        when(employeeService.getTopTenEmployees()).thenReturn(highest);
        String response = getResponse(get("/employee/top-ten-highest-earning"));

        assertEquals(highest, objectMapper.readValue(response, new TypeReference<List<String>>() {}));
    }

    @Test
    void createEmployee_givenCreateRequest_returnsCreatedEmployee() throws Exception {
        CreateEmployeeRequest createEmployeeRequest = CreateEmployeeRequest.builder()
                .name(EMPLOYEE_NAME)
                .age(25)
                .salary(1000)
                .title("Documentation Engineer")
                .build();
        when(employeeService.create(createEmployeeRequest)).thenReturn(EMPLOYEE);

        String json = objectMapper.writeValueAsString(createEmployeeRequest);

        String response = getResponse(
                post("/employee/create").contentType(APPLICATION_JSON).content(json));

        assertEquals(EMPLOYEE, objectMapper.readValue(response, Employee.class));
    }

    @Test
    void deleteEmployeeById_givenId_returnsNameOfTheEmployee() throws Exception {
        when(employeeService.delete(EMPLOYEE_ID_2)).thenReturn("Áki Ármannsson");

        String response = getResponse(delete("/employee/delete-by-id").param("id", EMPLOYEE_ID_2));

        assertEquals("Áki Ármannsson", response);
    }

    private String getResponse(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @AfterEach
    void afterEach() {
        String invocations = Mockito.mockingDetails(employeeService).printInvocations();
        assertThat(invocations).doesNotContainIgnoringCase("unused");
    }
}

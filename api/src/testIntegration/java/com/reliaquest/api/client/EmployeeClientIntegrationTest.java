package com.reliaquest.api.client;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.reliaquest.api.WiremockTest;
import com.reliaquest.api.client.api.DeleteEmployeeRequest;
import com.reliaquest.api.client.api.Response;
import com.reliaquest.api.controller.api.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class EmployeeClientIntegrationTest extends WiremockTest {

    private static final String EMPLOYEE_ID = "4579bc4e-9850-422b-b249-d2284cb887fd";

    @Test
    void getAllEmployees_callsExternal_returnsAll() {
        stubFor(get(urlEqualTo(EMPLOYEE_ENDPOINT))
                .willReturn(ok()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("get-all-employees-response.json")));

        List<Employee> allEmployees = employeeClient.getAll().data();

        assertEquals(employees(), allEmployees);
    }

    @Test
    void getById_givenId_returnsEmployee() {
        stubFor(get(urlEqualTo(EMPLOYEE_ENDPOINT + "/" + EMPLOYEE_ID))
                .willReturn(ok()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("get-by-id-response.json")));

        Employee actual = employeeClient.getById(EMPLOYEE_ID).data();

        assertEquals(employee(), actual);
    }

    @Test
    void create_givenCreateRequest_returnsCreatedEmployee() throws IOException {
        stubFor(post(urlEqualTo(EMPLOYEE_ENDPOINT))
                .withRequestBody(equalToJson(readJsonFile(CREATE_EMPLOYEE_REQUEST)))
                .willReturn(ok()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("create-employee-response.json")));

        CreateEmployeeRequest request = createEmployeeRequest();

        Employee actual = employeeClient.create(request).data();

        Employee expected = createdEmployee();

        assertEquals(expected, actual);
    }

    @Test
    void create_given429_returnsCreatedEmployee() throws IOException {
        stubFor(post(urlEqualTo(EMPLOYEE_ENDPOINT))
                .inScenario("Retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .withRequestBody(equalToJson(readJsonFile(CREATE_EMPLOYEE_REQUEST)))
                .willReturn(status(429)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody("Too many requests."))
                .willSetStateTo("Retry"));

        stubFor(post(urlEqualTo(EMPLOYEE_ENDPOINT))
                .inScenario("Retry")
                .whenScenarioStateIs("Retry")
                .withRequestBody(equalToJson(readJsonFile(CREATE_EMPLOYEE_REQUEST)))
                .willReturn(ok()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("create-employee-response.json")));

        CreateEmployeeRequest request = createEmployeeRequest();

        Employee actual = employeeClient.create(request).data();

        Employee expected = createdEmployee();

        assertEquals(expected, actual);

        verify(2, postRequestedFor(urlEqualTo(EMPLOYEE_ENDPOINT)));
    }

    @Test
    void delete_givenDeleteRequest_returnsSuccess() throws IOException {
        stubFor(delete(urlEqualTo(EMPLOYEE_ENDPOINT))
                .withRequestBody(equalToJson(readJsonFile(DELETE_EMPLOYEE_REQUEST)))
                .willReturn(ok()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("delete-employee-response.json")));

        Response<Boolean> response = employeeClient.delete(new DeleteEmployeeRequest("John Doe"));

        Response<Boolean> expected = new Response<>(true, "Successfully processed request.");

        assertEquals(expected, response);
    }
}

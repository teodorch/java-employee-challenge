package com.reliaquest.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.reliaquest.api.WiremockTest;
import com.reliaquest.api.controller.api.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class EmployeeControllerEnd2EndTest extends WiremockTest {


    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        stubFor(get(urlEqualTo(EMPLOYEE_ENDPOINT))
                .inScenario("end2end")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(ok()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("get-all-employees-response.json")));
        stubFor(get(urlEqualTo(EMPLOYEE_ENDPOINT))
                .inScenario("end2end")
                .whenScenarioStateIs("Created")
                .willReturn(ok()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("get-all-employees-after-new-response.json")));

        stubFor(post(urlEqualTo(EMPLOYEE_ENDPOINT))
                .withRequestBody(equalToJson(readJsonFile(CREATE_EMPLOYEE_REQUEST)))
                .inScenario("end2end")
                .willReturn(ok()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("create-employee-response.json"))
                .willSetStateTo("Created"));
    }

    @Test
    void givenEmployees_whenNewEmployeeCreated_recalculatesAll() throws Exception {
        String allEmployees = getResponse(MockMvcRequestBuilders.get("/employee/all"));

        assertThat(objectMapper.readValue(allEmployees, new TypeReference<List<Employee>>() {
        })).containsExactlyInAnyOrderElementsOf(employees());

        String highestSalary = getResponse(MockMvcRequestBuilders.get("/employee/highest-salary"));

        assertEquals(459912,
                objectMapper.readValue(highestSalary, Integer.class));

        String topTen = getResponse(MockMvcRequestBuilders.get("/employee/top-ten-highest-earning"));

        assertEquals(topTenBefore(), objectMapper.readValue(topTen, new TypeReference<List<String>>() {
        }));

        CreateEmployeeRequest request = createEmployeeRequest();
        String createJson = objectMapper.writeValueAsString(request);

        String newEmployeeJson = getResponse(MockMvcRequestBuilders.post("/employee/create")
                .contentType(APPLICATION_JSON_VALUE)
                .content(createJson));

        Employee newEmployee = objectMapper.readValue(newEmployeeJson, Employee.class);

        assertEquals(createdEmployee(), newEmployee);

        String byId = getResponse(MockMvcRequestBuilders.get("/employee/get-by-id")
                .param("id", newEmployee.id().toString()));

        assertEquals(newEmployee, objectMapper.readValue(byId, Employee.class));

        String highestSalaryNew = getResponse(MockMvcRequestBuilders.get("/employee/highest-salary"));

        assertEquals(500000,
                objectMapper.readValue(highestSalaryNew, Integer.class));

        String topTenNew = getResponse(MockMvcRequestBuilders.get("/employee/top-ten-highest-earning"));

        assertEquals(topTenNew(), objectMapper.readValue(topTenNew, new TypeReference<List<String>>() {
        }));
    }

    private List<String> topTenBefore() {
        return asList("Mrs. Vallie Wolff", "Chas Shields", "Mrs. Eleanora Predovic",
                "Áki Ármannsson", "Julie Zieme Sr.", "Blythe Kuhic");
    }

    private List<String> topTenNew() {
        return asList("John Doe", "Mrs. Vallie Wolff", "Chas Shields", "Mrs. Eleanora Predovic",
                "Áki Ármannsson", "Julie Zieme Sr.", "Blythe Kuhic");
    }

    private String getResponse(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

}
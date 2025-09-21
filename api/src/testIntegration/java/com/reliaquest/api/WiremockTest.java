package com.reliaquest.api;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.controller.api.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EnableWireMock({@ConfigureWireMock(port = 8888, filesUnderDirectory = "src/testIntegration/resources/")})
@ActiveProfiles("it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class WiremockTest {

    protected static final String EMPLOYEE_ENDPOINT = "/api/v1/employee";
    protected static final String CREATE_EMPLOYEE_REQUEST = "create-employee-request.json";
    protected static final String DELETE_EMPLOYEE_REQUEST = "delete-employee-request.json";

    @Autowired
    protected EmployeeClient employeeClient;

    protected String readJsonFile(String fileName) throws IOException {
        return Files.readString(Paths.get("src/testIntegration/resources/__files/" + fileName));
    }

    protected static CreateEmployeeRequest createEmployeeRequest() {
        return CreateEmployeeRequest.builder()
                .name("John Doe")
                .age(30)
                .salary(500000)
                .title("Software Engineer")
                .build();
    }

    protected List<Employee> employees() {
        return asList(employee(),
                new Employee(
                        UUID.fromString("b486d201-746f-4ef6-91e0-de1a85eae45b"),
                        "Blythe Kuhic",
                        152337,
                        29,
                        "Dynamic Healthcare Director",
                        "livlaughlo@company.com"
                ),
                new Employee(
                        UUID.fromString("65bb7ea7-a644-47b1-bde8-60433214c336"),
                        "Julie Zieme Sr.",
                        237428,
                        54,
                        "Customer Supervisor",
                        "teejay_thompson@company.com"
                ),
                new Employee(
                        UUID.fromString("594911ba-8c6a-4e22-af85-b889e43d3838"),
                        "Chas Shields",
                        406962,
                        26,
                        "Healthcare Architect",
                        "tickleme_pink@company.com"
                ),
                new Employee(
                        UUID.fromString("46080598-eb64-43b7-8ea6-0bcf07dc6cb4"),
                        "Mrs. Vallie Wolff",
                        459912,
                        31,
                        "Design Architect",
                        "omg_its_laura@company.com"
                ),
                new Employee(
                        UUID.fromString("e314c74d-e044-4985-80cb-b8222b11f239"),
                        "Áki Ármannsson",
                        250000,
                        35,
                        "Senior Software Engineer",
                        "aki.armannsson@company.com"
                )
        );
    }

    protected static Employee employee() {
        return new Employee(
                UUID.fromString("2a273517-b3a2-4b70-b46f-63b727d6a866"),
                "Mrs. Eleanora Predovic",
                278736,
                41,
                "Advertising Representative",
                "bitchin_blair@company.com"
        );
    }

    protected static Employee createdEmployee() {
        return new Employee(UUID.fromString("2dab2f20-1068-4f8e-b3d5-8fea02d65f18"),
                "John Doe", 500000, 30, "Software Engineer",
                "john_doe@company.com");
    }
}

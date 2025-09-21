package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.client.api.DeleteEmployeeRequest;
import com.reliaquest.api.client.api.Response;
import com.reliaquest.api.controller.api.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private static final String EMPLOYEE_ID = "f65040c9-7690-400f-9b19-3cab9c1497df";
    private static final String EMPLOYEE_NAME = "Billy Bob";
    private static final Employee EMPLOYEE = new Employee(
            UUID.fromString(EMPLOYEE_ID), EMPLOYEE_NAME, 1000, 25, "Documentation Engineer", "billy.bob@company.com");
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
    private static final String NEW_EMPLOYEE_ID = "d19a1323-caef-4280-bc78-95bab75c7827";
    private static final String NEW_EMPLOYEE_NAME = "Josh Billiam";
    private static final Employee NEW_EMPLOYEE = new Employee(
            UUID.fromString(NEW_EMPLOYEE_ID),
            NEW_EMPLOYEE_NAME,
            3000,
            25,
            "Solution Engineer",
            "josh.billiam@company.com");
    public static final CreateEmployeeRequest NEW_EMPLOYEE_REQUEST = CreateEmployeeRequest.builder()
            .age(25)
            .salary(1000)
            .title("Documentation Engineer")
            .name("John Doe")
            .build();

    @Mock
    private EmployeeClient employeeClient;

    @InjectMocks
    private CachedCalculationService employeeService;

    @BeforeEach
    void setUp() {
        CachedCalculationService.invalidateCache();
    }

    @Test
    void getAll_callsExternal_returnsAllEmployees() {
        when(employeeClient.getAll()).thenReturn(new Response<>(asList(EMPLOYEE, EMPLOYEE_2), STATUS));

        assertEquals(asList(EMPLOYEE_2, EMPLOYEE), employeeService.getAll());
    }

    @Test
    void getById_callsExternal_returnsEmployee() {
        when(employeeClient.getById(EMPLOYEE_ID)).thenReturn(new Response<>(EMPLOYEE, STATUS));

        assertEquals(Optional.of(EMPLOYEE), employeeService.getById(EMPLOYEE_ID));
    }

    @Test
    void getName_givenNameFragment_findsEmployee() {
        when(employeeClient.getAll()).thenReturn(new Response<>(asList(EMPLOYEE, EMPLOYEE_2, NEW_EMPLOYEE), STATUS));

        assertThat(employeeService.getByName("Bill"))
                .containsExactlyInAnyOrderElementsOf(asList(EMPLOYEE, NEW_EMPLOYEE));
    }

    @Test
    void create_callsExternal_returnsEmployee() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .age(25)
                .salary(3000)
                .title("Documentation Engineer")
                .name(NEW_EMPLOYEE_NAME)
                .build();
        when(employeeClient.create(request)).thenReturn(new Response<>(NEW_EMPLOYEE, STATUS));

        assertEquals(NEW_EMPLOYEE, employeeService.create(request));
    }

    @Test
    void delete_callsExternal_returnsEmployeeName() {
        when(employeeClient.getById(EMPLOYEE_ID)).thenReturn(new Response<>(EMPLOYEE, STATUS));
        DeleteEmployeeRequest request = new DeleteEmployeeRequest(EMPLOYEE_NAME);
        when(employeeClient.delete(request)).thenReturn(new Response<>(true, STATUS));

        assertEquals(EMPLOYEE_NAME, employeeService.delete(EMPLOYEE_ID));
    }

    @Test
    void delete_externalReturnsFalse_throwsException() {
        when(employeeClient.getById(EMPLOYEE_ID)).thenReturn(new Response<>(EMPLOYEE, STATUS));
        DeleteEmployeeRequest request = new DeleteEmployeeRequest(EMPLOYEE_NAME);
        when(employeeClient.delete(request)).thenReturn(new Response<>(false, STATUS));

        assertThrows(RuntimeException.class, () -> employeeService.delete(EMPLOYEE_ID));
    }

    @Test
    void getHighestSalary_returnsHighestSalary() {
        when(employeeClient.getAll()).thenReturn(new Response<>(asList(EMPLOYEE, EMPLOYEE_2), STATUS));

        assertEquals(2000, employeeService.getHighestSalary());
    }

    @Test
    void getTopTenEmployeeNames_returnsTopTenEmployeeNames() {
        when(employeeClient.getAll()).thenReturn(new Response<>(asList(EMPLOYEE, EMPLOYEE_2), STATUS));

        assertEquals(asList(EMPLOYEE_NAME_2, EMPLOYEE_NAME), employeeService.getTopTenEmployees());
    }

    // cache tests

    @Test
    void getAll_calledTwice_callsExternalOnce() {
        when(employeeClient.getAll()).thenReturn(new Response<>(asList(EMPLOYEE, EMPLOYEE_2), STATUS));

        employeeService.getAll();
        employeeService.getAll();

        verify(employeeClient).getAll();
    }

    @Test
    void getById_givenPopulatedCache_doesntCallExternalOnce() {
        when(employeeClient.getAll()).thenReturn(new Response<>(asList(EMPLOYEE, EMPLOYEE_2), STATUS));

        employeeService.getAll();

        employeeService.getById(EMPLOYEE_ID);
        employeeService.getById(EMPLOYEE_ID);

        verify(employeeClient, never()).getById(EMPLOYEE_ID);
    }

    @Test
    void getByName_calledTwice_callsExternalOnce() {
        when(employeeClient.getAll()).thenReturn(new Response<>(asList(EMPLOYEE, EMPLOYEE_2), STATUS));

        employeeService.getByName(EMPLOYEE_NAME);
        employeeService.getByName(EMPLOYEE_NAME);

        verify(employeeClient).getAll();
    }

    @Test
    void getHighestSalary_calledTwice_callsExternalOnce() {
        when(employeeClient.getAll()).thenReturn(new Response<>(asList(EMPLOYEE, EMPLOYEE_2), STATUS));

        employeeService.getHighestSalary();
        employeeService.getHighestSalary();

        verify(employeeClient).getAll();
    }

    @Test
    void getTopTen_calledTwice_callsExternalOnce() {
        when(employeeClient.getAll()).thenReturn(new Response<>(asList(EMPLOYEE, EMPLOYEE_2), STATUS));

        employeeService.getTopTenEmployees();
        employeeService.getTopTenEmployees();

        verify(employeeClient).getAll();
    }

    @Test
    void create_givenPopulatedCache_updatesCache() {
        when(employeeClient.getAll()).thenReturn(new Response<>(asList(EMPLOYEE, EMPLOYEE_2), STATUS));

        when(employeeClient.create(NEW_EMPLOYEE_REQUEST)).thenReturn(new Response<>(NEW_EMPLOYEE, STATUS));

        assertEquals(asList(EMPLOYEE_2, EMPLOYEE), employeeService.getAll());
        assertEquals(asList(EMPLOYEE_NAME_2, EMPLOYEE_NAME), employeeService.getTopTenEmployees());
        assertEquals(2000, employeeService.getHighestSalary());

        employeeService.create(NEW_EMPLOYEE_REQUEST);
        employeeService.getById(NEW_EMPLOYEE_ID);

        verify(employeeClient, never()).getById(NEW_EMPLOYEE_ID);
        verify(employeeClient).getAll();
        assertEquals(3000, employeeService.getHighestSalary());
        assertEquals(asList(NEW_EMPLOYEE_NAME, EMPLOYEE_NAME_2, EMPLOYEE_NAME), employeeService.getTopTenEmployees());
    }

    @Test
    void delete_givenPopulatedCache_recalculatesCache() {
        when(employeeClient.getAll()).thenReturn(new Response<>(asList(EMPLOYEE, EMPLOYEE_2, NEW_EMPLOYEE), STATUS));
        DeleteEmployeeRequest deleteEmployeeRequest = new DeleteEmployeeRequest(NEW_EMPLOYEE_NAME);
        when(employeeClient.delete(deleteEmployeeRequest)).thenReturn(new Response<>(true, STATUS));

        assertThat(employeeService.getAll()).containsExactlyInAnyOrderElementsOf(asList(EMPLOYEE_2, EMPLOYEE, NEW_EMPLOYEE));
        assertEquals(asList(NEW_EMPLOYEE_NAME, EMPLOYEE_NAME_2, EMPLOYEE_NAME), employeeService.getTopTenEmployees());
        assertEquals(3000, employeeService.getHighestSalary());
        assertEquals(Optional.of(EMPLOYEE_2), employeeService.getById(EMPLOYEE_ID_2));
        assertEquals(singletonList(EMPLOYEE_2), employeeService.getByName(EMPLOYEE_NAME_2));

        employeeService.delete(NEW_EMPLOYEE_ID);


        assertThat(employeeService.getAll()).containsExactlyInAnyOrderElementsOf(asList(EMPLOYEE_2, EMPLOYEE));
        assertEquals(asList(EMPLOYEE_NAME_2, EMPLOYEE_NAME), employeeService.getTopTenEmployees());
        assertEquals(2000, employeeService.getHighestSalary());
        assertEquals(Optional.of(EMPLOYEE_2), employeeService.getById(EMPLOYEE_ID_2));
        assertEquals(singletonList(EMPLOYEE_2), employeeService.getByName(EMPLOYEE_NAME_2));

        verify(employeeClient).getAll();
    }

    @Test
    void delete_clearsSearchCache() {
        when(employeeClient.getAll()).thenReturn(new Response<>(singletonList(EMPLOYEE), STATUS));
        DeleteEmployeeRequest deleteEmployeeRequest = new DeleteEmployeeRequest(EMPLOYEE_NAME);
        when(employeeClient.delete(deleteEmployeeRequest)).thenReturn(new Response<>(true, STATUS));

        employeeService.getAll();

        assertTrue(CachedCalculationService.getSEARCH_CACHE().isEmpty());

        employeeService.getByName("Bill");

        assertTrue(CachedCalculationService.getSEARCH_CACHE().containsKey("bill"));

        employeeService.delete(EMPLOYEE_ID);

        assertTrue(CachedCalculationService.getSEARCH_CACHE().isEmpty());
    }

    @Test
    void create_clearsSearchCache() {
        when(employeeClient.getAll()).thenReturn(new Response<>(singletonList(EMPLOYEE), STATUS));
        when(employeeClient.create(NEW_EMPLOYEE_REQUEST)).thenReturn(new Response<>(NEW_EMPLOYEE, STATUS));

        employeeService.getAll();

        assertTrue(CachedCalculationService.getSEARCH_CACHE().isEmpty());

        employeeService.getByName("Bill");

        assertTrue(CachedCalculationService.getSEARCH_CACHE().containsKey("bill"));

        employeeService.create(NEW_EMPLOYEE_REQUEST);

        assertTrue(CachedCalculationService.getSEARCH_CACHE().isEmpty());
    }
}
package com.capg.payrollservice;

import static org.junit.Assert.assertEquals;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import org.junit.Test;

import com.capg.payrollservice.EmployeePayrollService.IOService;
import com.capg.payrollserviceJDBC.DatabaseException;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.junit.Before;
import org.junit.Test;

public class EmployeePayrollServiceTest {

	@Test
	public void givenEmployeePayrollInDB_WhenRetrieved_ShouldMatchEmployeeCount() throws DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<Employee> employeePayrollData = employeePayrollService.readEmployeePayrollDBData(IOService.DB_IO);
		assertEquals(2, employeePayrollData.size());
	}

	@Test
	public void givenEmployeePayrollInDB_WhenRetrievedForDateRange_ShouldMatchEmployeeCount() throws DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollDBData(IOService.DB_IO);
		List<Employee> resultList = employeePayrollService.getEmployeeForDateRange(LocalDate.of(2020, 01, 01),
				LocalDate.of(2021, 01, 01));
		assertEquals(2, resultList.size());
	}

	@Test
	public void givenEmployeePayrollInDB_WhenRetrievedForAverage_ShouldMatchedAverageSalaryForGender()
			throws DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		Map<String, Double> genderComputedMap = null;
		genderComputedMap = employeePayrollService.getSalaryAverageByGender();
		assertEquals(true, genderComputedMap.get("M") == 2000000);
		assertEquals(true, genderComputedMap.get("F") == 5000000);

	}

	@Test
	public void givenEmployeePayrollInDB_WhenRetrievedForSum_ShouldMatchedSumSalaryForGender()
			throws DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		Map<String, Double> genderComputedMap = null;
		genderComputedMap = employeePayrollService.getSalarySumByGender();
		assertEquals(true, genderComputedMap.get("M") == 6000000);
		assertEquals(true, genderComputedMap.get("F") == 5000000);

	}

	@Test
	public void givenEmployees_WhenRetrievedForMin_ShouldMatchedMinSalaryForGender() throws DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		Map<String, Double> genderComputedMap = null;
		genderComputedMap = employeePayrollService.getMinSalaryByGender();
		assertEquals(true, genderComputedMap.get("M") == 1000000);
		assertEquals(true, genderComputedMap.get("F") == 5000000);

	}

	@Test
	public void givenEmployees_WhenRetrievedForMax_ShouldMatchedMaxSalaryForGender() throws DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		Map<String, Double> genderComputedMap = null;
		genderComputedMap = employeePayrollService.getMaxSalaryByGender();
		assertEquals(true, genderComputedMap.get("M") == 3000000);
		assertEquals(true, genderComputedMap.get("F") == 5000000);

	}

	@Test
	public void givenEmployees_WhenRetrievedForCount_ShouldMatchedCountForGender() throws DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		Map<String, Double> genderComputedMap = null;
		genderComputedMap = employeePayrollService.getCountByGender();
		assertEquals(true, genderComputedMap.get("M") == 3);
		assertEquals(true, genderComputedMap.get("F") == 1);

	}

	@Test
	public void givenNewEmployee_WhenAdded_ShouldSyncWithDB() throws SQLException, DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollDBData(IOService.DB_IO);
		employeePayrollService.addEmployeeToPayrollAndDepartment("James", "M", 5000000.0, LocalDate.now(),
				Arrays.asList("Marketing"));
		boolean result = employeePayrollService.checkEmployeeDataSync("James");
		assertEquals(true, result);
	}

	@Test
	public void givenEmployeeDB_WhenAnEmployeeIsDeleted_ShouldSyncWithDB() throws DatabaseException {
		EmployeePayrollService employeeService = new EmployeePayrollService();
		employeeService.readEmployeePayrollDBData(IOService.DB_IO);
		List<Employee> list = employeeService.deleteEmployee("Mark");
		assertEquals(3, list.size());
	}

	@Test
	public void givenNewEmployee_WhenAddedToPayroll_ShouldBeAddedToDepartment() throws SQLException, DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollDBData(IOService.DB_IO);
		employeePayrollService.addEmployeeToPayrollAndDepartment("Mark", "M", 5000000.0, LocalDate.now(),
				Arrays.asList("Sales,Marketing"));
		boolean result = employeePayrollService.checkEmployeeDataSync("Mark");
		assertEquals(true, result);
	}

	@Test
	public void givenEmployeeId_WhenRemoved_shouldReturnNumberOfActiveEmployees() throws DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<Employee> onlyActiveList = employeePayrollService.removeEmployeeFromPayroll(3);
		assertEquals(3, onlyActiveList.size());
	}

	@Test
	public void geiven6Employees_WhenAddedToDB_ShouldMatchEmployeeEntries() throws DatabaseException {
		Employee[] arrayOfEmp = { new Employee(0, "Jeff Bezos", 100000.0, "M", LocalDate.now(), Arrays.asList("Sales")),
				new Employee(0, "Bill Gates", 200000.0, "M", LocalDate.now(), Arrays.asList("Marketing")),
				new Employee(0, "Mark", 150000.0, "M", LocalDate.now(), Arrays.asList("Technical")),
				new Employee(0, "Sundar", 400000.0, "M", LocalDate.now(), Arrays.asList("Sales,Technical")),
				new Employee(0, "Mukesh", 4500000.0, "M", LocalDate.now(), Arrays.asList("Sales")),
				new Employee(0, "Anil", 300000.0, "M", LocalDate.now(), Arrays.asList("Sales")) };
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollDBData(IOService.DB_IO);
		Instant start = Instant.now();
		employeePayrollService.addEmployeesToPayroll(Arrays.asList(arrayOfEmp));
		Instant end = Instant.now();
		System.out.println("Duration without Thread: " + Duration.between(start, end));
		Instant threadStart = Instant.now();
		employeePayrollService.addEmployeesToPayrollWithThreads(Arrays.asList(arrayOfEmp));
		Instant threadEnd = Instant.now();
		System.out.println("Duration with Thread: " + Duration.between(threadStart, threadEnd));
		long result = employeePayrollService.countEntries(IOService.DB_IO);
		assertEquals(13, result);
	}

	@Test
	public void geiven2Employees_WhenUpdatedSalary_ShouldSyncWithDB() throws DatabaseException {
		Map<String, Double> salaryMap = new HashMap<>();
		salaryMap.put("Bill Gates", 700000.0);
		salaryMap.put("Mukesh", 800000.0);
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollDBData(IOService.DB_IO);
		Instant start = Instant.now();
		employeePayrollService.updatePayroll(salaryMap, IOService.DB_IO);
		Instant end = Instant.now();
		System.out.println("Duration with Thread: " + Duration.between(start, end));
		boolean result = employeePayrollService.checkEmployeeListSync(Arrays.asList("Mukesh"));
		assertEquals(true, result);
	}

	@Before
	public void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	private Employee[] getEmployeeList() {
		Response response = RestAssured.get("/employees");
		System.out.println("Employee payroll entries in JSONServer:\n" + response.asString());
		Employee[] arrayOfEmp = new Gson().fromJson(response.asString(), Employee[].class);
		return arrayOfEmp;
	}

	private Response addEmployeeToJsonServer(Employee employee) {
		String empJson = new Gson().toJson(employee);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.post("/employees");
	}
}
package com.payrollservice;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import com.payrollservice.Employee;
import com.payrollservice.EmployeePayrollService;
import com.payrollservice.EmployeePayrollService.IOService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class EmployeePayrollServiceTest {
	/**
	 * UC2
	 * 
	 * @throws SQLException
	 */
	@Test
	public void givenEmployeePayrollInDB_WhenRetrieved_ShouldMatchEmployeeCount() throws SQLException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<Employee> employeePayrollData;
		try {
			employeePayrollData = employeePayrollService.readEmployeePayrollDBData(IOService.DB_IO);
			assertEquals(5, employeePayrollData.size());
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * UC3
	 * 
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	@Test
	public void givenNewSalary_WhenUpdated_ShouldBeInSync() throws DatabaseException, SQLException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<Employee> employeePayrollData = employeePayrollService.readEmployeePayrollDBData(IOService.DB_IO);
		employeePayrollService.updateEmployeeSalary("Deepika", 5000000);
		employeePayrollService.readEmployeePayrollDBData(EmployeePayrollService.IOService.DB_IO);
		boolean result = employeePayrollService.checkEmployeeDataSync("Deepika");
		assertEquals(true, result);
	}

	/**
	 * UC4
	 * 
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	@Test
	public void givenNewSalaryForEmployee_WhenUpdated_ShouldBeInSync() throws DatabaseException, SQLException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<Employee> employeePayrollData = employeePayrollService.readEmployeePayrollDBData(IOService.DB_IO);
		employeePayrollService.updateEmployeeSalary("Terissa", 5000000);
		employeePayrollService.readEmployeePayrollDBData(EmployeePayrollService.IOService.DB_IO);
		boolean result = employeePayrollService.checkEmployeeDataSync("Terissa");
		assertEquals(true, result);
	}

	/**
	 * UC5
	 * 
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	@Test
	public void givenEmployeePayrollInDB_WhenRetrievedForDateRange_ShouldMatchEmployeeCount()
			throws DatabaseException, SQLException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<Employee> employeePayrollData = employeePayrollService.readEmployeePayrollDBData(IOService.DB_IO);
		List<Employee> resultList = employeePayrollService.getEmployeeForDateRange(LocalDate.of(2019, 01, 01),
				LocalDate.of(2020, 01, 01));
		assertEquals(3, resultList.size());
	}

	/**
	 * UC6
	 * 
	 * @throws DatabaseException
	 */
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

	/**
	 * UC7 and UC8
	 * 
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	@Test
	public void givenNewEmployee_WhenAdded_ShouldSyncWithDB() throws SQLException, DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		employeePayrollService.addEmployeeToPayrollAndDepartment("Mark", "M", 5000000.0, LocalDate.now(), "Marketing");
		boolean result = employeePayrollService.checkEmployeeDataSync("Mark");
		assertEquals(true, result);
	}

	/**
	 * UC9: Inserting data according to new database structure Usecase11: UC11:
	 * Refactored for the single transaction
	 * 
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	@Test
	public void givenNewEmployee_WhenAddedToPayroll_ShouldBeAddedToDepartment() throws SQLException, DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		employeePayrollService.addEmployeeToPayrollAndDepartment("Mark", "M", 5000000.0, LocalDate.now(), "Sales");
		boolean result = employeePayrollService.checkEmployeeDataSync("Mark");
		assertEquals(true, result);
	}

	/**
	 * UC12: Remove employee from payroll
	 * 
	 * @throws DatabaseException
	 */
	@Test
	void givenEmployeeId_WhenRemoved_shouldReturnNumberOfActiveEmployees() throws DatabaseException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<Employee> onlyActiveList = employeePayrollService.removeEmployeeFromPayroll(3);
		assertEquals(3, onlyActiveList.size());
	}
}
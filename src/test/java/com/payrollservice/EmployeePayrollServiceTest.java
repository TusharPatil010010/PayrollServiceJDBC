package com.payrollservice;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import com.payrollservice.Employee;
import com.payrollservice.EmployeePayrollService;
import com.payrollservice.EmployeePayrollService.IOService;

import java.sql.SQLException;
import java.util.*;

public class EmployeePayrollServiceTest {
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

	@Test
	public void givenNewSalary_WhenUpdated_ShouldBeInSync() throws DatabaseException, SQLException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<Employee> employeePayrollData = employeePayrollService.readEmployeePayrollDBData(IOService.DB_IO);
		employeePayrollService.updateEmployeeSalary("Deepika", 5000000);
		employeePayrollService.readEmployeePayrollDBData(EmployeePayrollService.IOService.DB_IO);
		boolean result = employeePayrollService.checkEmployeeDataSync("Deepika");
		assertEquals(true, result);
	}
}
package com.payrollservice;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class EmployeePayrollService {
	static Scanner consoleInput = new Scanner(System.in);

	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	};

	private List<Employee> employeeList = new ArrayList<>();
	private PayrollServiceJDBC employeePayrollDB;

	public EmployeePayrollService(List<Employee> list) {
		this();
		this.employeeList = list;
	}

	public EmployeePayrollService() {
		employeePayrollDB = PayrollServiceJDBC.getInstance();
	}

	public static void main(String[] args) {
		ArrayList<Employee> list = new ArrayList<Employee>();
		EmployeePayrollService eService = new EmployeePayrollService(list);
		System.out.println("Do you want to add data from console");
		String option = consoleInput.nextLine();
		do {
			if (option.equalsIgnoreCase("yes")) {
				eService.readEmployeePayrollData(IOService.CONSOLE_IO);
				consoleInput.nextLine();
				System.out.println("Want to enter again");
				option = consoleInput.nextLine();
			}
		} while (option.equalsIgnoreCase("yes"));
		eService.writeData(IOService.FILE_IO);
		eService.readEmployeePayrollData(IOService.FILE_IO);
	}

	public void writeData(IOService ioService) {
		if (ioService.equals(IOService.CONSOLE_IO))
			System.out.println("Writting data of employee to console: " + employeeList);
		else if (ioService.equals(IOService.FILE_IO)) {
			new PayrollFileServiceIO().writeData(employeeList);
		}
	}

	public void readEmployeePayrollData(IOService ioService) {
		List<Employee> list = new ArrayList<>();
		if (ioService.equals(IOService.CONSOLE_IO)) {
			System.out.println("Enter the employee id");
			int id = consoleInput.nextInt();
			consoleInput.nextLine();
			System.out.println("Enter the employee name");
			String name = consoleInput.nextLine();
			System.out.println("Enter the employee salary");
			double salary = consoleInput.nextDouble();
			employeeList.add(new Employee(id, name, salary));
		} else if (ioService.equals(IOService.FILE_IO)) {
			list = new PayrollFileServiceIO().readData();
			System.out.println("Writing data from file" + list);
		}
	}

	public void printData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO)) {
			new PayrollFileServiceIO().printData();
		}
	}

	public long countEntries(IOService ioService) {
		long entries = 0;
		if (ioService.equals(IOService.FILE_IO)) {
			entries = new PayrollFileServiceIO().countEntries();
		}
		System.out.println("No of Entries in File: " + entries);
		return entries;
	}

	public List<Employee> readEmployeePayrollDBData(IOService ioService) throws DatabaseException, SQLException {
		if (ioService.equals(IOService.DB_IO)) {
			this.employeeList = employeePayrollDB.readData();
		}
		return this.employeeList;
	}

	public void updateEmployeeSalary(String name, double salary) throws DatabaseException {
		int result = employeePayrollDB.updateEmployeeData(name, salary);
		if (result == 0)
			return;
		Employee employee = this.getEmployee(name);
		if (employee != null)
			employee.salary = salary;
	}

	private Employee getEmployee(String name) {
		Employee employee = this.employeeList.stream().filter(employeeData -> employeeData.name.equals(name))
				.findFirst().orElse(null);
		return employee;
	}

	public boolean checkEmployeeDataSync(String name) throws DatabaseException {
		List<Employee> employeeList = employeePayrollDB.getEmployeePayrollData(name);
		return employeeList.get(0).equals(getEmployee(name));
	}

	public List<Employee> getEmployeeForDateRange(LocalDate start, LocalDate end) throws DatabaseException {
		return employeePayrollDB.getEmployeeForDateRange(start, end);
	}

	public Map<String, Double> getSalaryAverageByGender() throws DatabaseException {
		return employeePayrollDB.getEmployeesByFunction("AVG");
	}

	public Map<String, Double> getSalarySumByGender() throws DatabaseException {
		return employeePayrollDB.getEmployeesByFunction("SUM");
	}

	public Map<String, Double> getMinSalaryByGender() throws DatabaseException {
		return employeePayrollDB.getEmployeesByFunction("MIN");
	}

	public Map<String, Double> getMaxSalaryByGender() throws DatabaseException {
		return employeePayrollDB.getEmployeesByFunction("MAX");
	}

	public Map<String, Double> getCountByGender() throws DatabaseException {
		return employeePayrollDB.getEmployeesByFunction("COUNT");
	}

	public void addEmployeeToPayroll(String name, String gender, double salary, LocalDate start)
			throws SQLException, DatabaseException {
		this.employeeList.add(employeePayrollDB.addEmployeeToPayroll(name, gender, salary, start));
	}

	public List<Employee> deleteEmployee(String name) throws DatabaseException, SQLException {
		employeePayrollDB.deleteEmployee(name);
		return readEmployeePayrollDBData(IOService.DB_IO);

	}

	public void addEmployeeToDepartment(String name, String gender, double salary, LocalDate start, String department)
			throws SQLException, DatabaseException {
		this.employeeList.add(employeePayrollDB.addEmployeeToDepartment(name, gender, salary, start, department));
	}

}

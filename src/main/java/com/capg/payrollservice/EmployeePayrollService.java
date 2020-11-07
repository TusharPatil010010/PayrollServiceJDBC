package com.capg.payrollservice;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.capg.payrollserviceJDBC.DatabaseException;
import com.capg.payrollserviceJDBC.EmployeePayrollDB;

public class EmployeePayrollService {
	private static final Logger LOG = LogManager.getLogger(EmployeePayrollDB.class);
	static Scanner consoleInput = new Scanner(System.in);

	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	};

	private List<Employee> employeeList = new ArrayList<>();
	private EmployeePayrollDB employeePayrollDB;

	public EmployeePayrollService(List<Employee> list) {
		this();
		this.employeeList = new ArrayList<>(list);
	}

	public EmployeePayrollService() {
		employeePayrollDB = EmployeePayrollDB.getInstance();
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

	/**
	 * Writes data through file IO
	 * 
	 * @param ioService
	 */
	public void writeData(IOService ioService) {
		if (ioService.equals(IOService.CONSOLE_IO))
			System.out.println("Writting data of employee to console: " + employeeList);
		else if (ioService.equals(IOService.FILE_IO)) {
			new EmployeeFileService().writeData(employeeList);
		}
	}

	/**
	 * Reads data through file IO
	 * 
	 * @param ioService
	 */
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
			list = new EmployeeFileService().readData();
			System.out.println("Writing data from file" + list);
		}
	}

	/**
	 * Prints data through file IO
	 * 
	 * @param ioService
	 */
	public void printData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO)) {
			new EmployeeFileService().printData();
		}
	}

	/**
	 * Counts entries through file IO
	 * 
	 * @param ioService
	 * @return
	 */
	public long countEntries(IOService ioService) {
		long entries = 0;
		if (ioService.equals(IOService.FILE_IO)) {
			entries = new EmployeeFileService().countEntries();
		} else {
			entries = employeeList.size();
		}
		return entries;
	}

	/**
	 * UC2: Reading data from database table
	 * 
	 * @param ioService
	 * @return
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public List<Employee> readEmployeePayrollDBData(IOService ioService) throws DatabaseException {
		if (ioService.equals(IOService.DB_IO)) {
			employeeList = employeePayrollDB.readData();
		}
		return employeeList;
	}

	/**
	 * Gets employee by name
	 * 
	 * @param name
	 * @return
	 */
	public Employee getEmployee(String name) {
		Employee employee = this.employeeList.stream().filter(employeeData -> employeeData.name.equals(name))
				.findFirst().orElse(null);
		return employee;
	}

	/**
	 * Checks sync after updation
	 * 
	 * @param name
	 * @return
	 * @throws DatabaseException
	 */
	public boolean checkEmployeeDataSync(String name) throws DatabaseException {
		List<Employee> employeeList = employeePayrollDB.getEmployeePayrollData(name);
		return employeeList.get(0).equals(getEmployee(name));
	}

	/**
	 * UC5: to retrieve employees in the particular dates
	 * 
	 * @throws DatabaseException
	 */
	public List<Employee> getEmployeeForDateRange(LocalDate start, LocalDate end) throws DatabaseException {
		return employeePayrollDB.getEmployeeForDateRange(start, end);
	}

	/**
	 * UC6: to perform aggregate functions on the employee table
	 * 
	 * @throws DatabaseException
	 */
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

	/**
	 * UC7: To insert new Employee to the table..................................
	 * UC9: Inserting data according to new database structure...................
	 * UC11: Refactored for the single transaction
	 * 
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public void addEmployeeToPayrollAndDepartment(String name, String gender, double salary, LocalDate start,
			List<String> department) throws SQLException, DatabaseException {
		this.employeeList
				.add(employeePayrollDB.addEmployeeToPayrollAndDepartment(name, gender, salary, start, department));
	}

	/**
	 * UC8: performing the cascading delete operation on database
	 * 
	 * @throws DatabaseException
	 */
	public List<Employee> deleteEmployee(String name) throws DatabaseException {
		employeePayrollDB.deleteEmployee(name);
		return readEmployeePayrollDBData(IOService.DB_IO);

	}

	/**
	 * Removes employee from payroll by id
	 * 
	 * @param id
	 * @return
	 * @throws DatabaseException
	 */
	public List<Employee> removeEmployeeFromPayroll(int id) throws DatabaseException {
		List<Employee> activeList = null;
		activeList = employeePayrollDB.removeEmployeeFromCompany(id);
		return activeList;
	}

	/**
	 * Adds employee to employee payroll
	 * 
	 * @param employeeDataList
	 */
	public void addEmployeesToPayroll(List<Employee> employeeDataList) {
		employeeDataList.forEach(employee -> {
			System.out.println("Employee Being added: " + employee.name);
			try {
				this.addEmployeeToPayrollAndDepartment(employee.name, employee.gender, employee.salary, employee.start,
						employee.department);
			} catch (SQLException | DatabaseException e) {
				e.printStackTrace();
			}
			System.out.println("Employee added: " + employee.name);
		});
		System.out.println(this.employeeList);

	}

	/**
	 * UC14: Adding employees to table using threads in less time Usecase15: Thread
	 * execution and synchronization
	 * 
	 * @param employeeDataList
	 */
	public void addEmployeesToPayrollWithThreads(List<Employee> employeeDataList) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<Integer, Boolean>();
		employeeDataList.forEach(employee -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(employee.hashCode(), false);
				LOG.info("Employee Being Added: " + Thread.currentThread().getName());
				try {
					this.addEmployeeToPayrollAndDepartment(employee.name, employee.gender, employee.salary,
							employee.start, employee.department);
				} catch (SQLException | DatabaseException e) {
					e.printStackTrace();
				}
				employeeAdditionStatus.put(employee.hashCode(), true);
				LOG.info("Employee Added: " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, employee.name);
			thread.start();
		});
		while (employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * UC17 : Updating the salary in table using the multithreading
	 * 
	 * @param salaryMap
	 */
	public void updatePayroll(Map<String, Double> salaryMap, IOService ioService) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<Integer, Boolean>();
		salaryMap.forEach((k, v) -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(k.hashCode(), false);
				LOG.info("Employee Being Added: " + Thread.currentThread().getName());
				try {
					this.updatePayrollDB(k, v, ioService);
				} catch (DatabaseException | SQLException e) {
					e.printStackTrace();
				}
				employeeAdditionStatus.put(k.hashCode(), true);
				LOG.info("Employee Added: " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, k);
			thread.start();
		});
		while (employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Updates payroll database
	 * 
	 * @param name
	 * @param salary
	 * @param ioService
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public void updatePayrollDB(String name, Double salary, IOService ioService)
			throws DatabaseException, SQLException {
		if (ioService.equals(IOService.DB_IO)) {
			int result = employeePayrollDB.updateEmployeePayrollData(name, salary);
			if (result == 0)
				return;
		}
		Employee employee = this.getEmployee(name);
		if (employee != null)
			employee.salary = salary;
	}

	/**
	 * Checks sync after updation
	 * 
	 * @param nameList
	 * @return
	 * @throws DatabaseException
	 */
	public boolean checkEmployeeListSync(List<String> nameList) throws DatabaseException {
		List<Boolean> resultList = new ArrayList<>();
		nameList.forEach(name -> {
			List<Employee> employeeList;
			try {
				employeeList = employeePayrollDB.getEmployeePayrollData(name);
				resultList.add(employeeList.get(0).equals(getEmployee(name)));
			} catch (DatabaseException e) {
			}
		});
		if (resultList.contains(false)) {
			return false;
		}
		return true;
	}

	/**
	 * REST Json UC1: adding the employee to cache
	 * 
	 * @param employee
	 */
	public void addEmployeeToPayroll(Employee employee) {
		employeeList.add(employee);
	}

	/**
	 * REST Json UC5: deleting employee from the cache as well as JSON Server
	 * 
	 * @param name
	 * @param ioService
	 */
	public void deleteEmployee(String name, IOService ioService) {
		if (ioService.equals(IOService.REST_IO)) {
			Employee employee = this.getEmployee(name);
			employeeList.remove(employee);
		}
	}
}
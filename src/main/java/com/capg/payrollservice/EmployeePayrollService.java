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

	public void writeData(IOService ioService) {
		if (ioService.equals(IOService.CONSOLE_IO))
			System.out.println("Writting data of employee to console: " + employeeList);
		else if (ioService.equals(IOService.FILE_IO)) {
			new EmployeeFileService().writeData(employeeList);
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
			list = new EmployeeFileService().readData();
			System.out.println("Writing data from file" + list);
		}
	}

	public void printData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO)) {
			new EmployeeFileService().printData();
		}
	}

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
	 * Usecase2: Reading data from database table
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

	public Employee getEmployee(String name) {
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

	public void addEmployeeToPayrollAndDepartment(String name, String gender, double salary, LocalDate start,
			List<String> department) throws SQLException, DatabaseException {
		this.employeeList
				.add(employeePayrollDB.addEmployeeToPayrollAndDepartment(name, gender, salary, start, department));
	}

	public List<Employee> deleteEmployee(String name) throws DatabaseException {
		employeePayrollDB.deleteEmployee(name);
		return readEmployeePayrollDBData(IOService.DB_IO);

	}

	public List<Employee> removeEmployeeFromPayroll(int id) throws DatabaseException {
		List<Employee> activeList = null;
		activeList = employeePayrollDB.removeEmployeeFromCompany(id);
		return activeList;
	}

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
	 * Usecase14: Adding employees to table using threads in less time Usecase15:
	 * Thread execution and synchronization
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
	 * Usecase17 : Updating the salary in table using the multithreading
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
	 * Json Usecase1: adding the employee to cache
	 * 
	 * @param employee
	 */
	public void addEmployeeToPayroll(Employee employee) {
		employeeList.add(employee);
	}
}
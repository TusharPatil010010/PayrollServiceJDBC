package com.payrollservice;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PayrollServiceJDBC {

	private PreparedStatement employeeStatement;
	private static PayrollServiceJDBC employeePayrollDB;

	public PayrollServiceJDBC() {
	}

	public static PayrollServiceJDBC getInstance() {
		if (employeePayrollDB == null) {
			employeePayrollDB = new PayrollServiceJDBC();
		}
		return employeePayrollDB;
	}

	private Connection getConnection() throws DatabaseException {
		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String userName = "root";
		String password = "open";
		Connection connection = null;
		// loading the driver
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Driver Loaded Successfully.");
		} catch (ClassNotFoundException exception) {
			throw new IllegalStateException("Cannot find driver in the classpath...", exception);
		}
		listDrivers();
		// creating a connection
		try {
			System.out.println("Connecting to database..... " + jdbcURL);
			connection = DriverManager.getConnection(jdbcURL, userName, password);
			System.out.println("Connection is successful: " + connection);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return connection;
	}

	private static void listDrivers() {
		Enumeration<Driver> driverList = DriverManager.getDrivers();
		while (driverList.hasMoreElements()) {
			Driver driverClass = (Driver) driverList.nextElement();
			System.out.println("  " + driverClass.getClass().getName());
		}
	}

	/**
	 * UC2, UC10: Reading data from the employee_payroll_service
	 * 
	 * @return
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public List<Employee> readData() throws DatabaseException {
		String sql = "Select id,name,gender,salary,start,department.department_name from employee_payroll_service "
				+ "inner join department on employee_payroll_service.id = department.employee_id; ";
		return this.getEmployeePayrollAndDeparmentData(sql);
	}

	private int updateEmployeeUsingStatement(String name, double salary) throws DatabaseException {
		String sql = String.format("Update employee_payroll_service set salary = %.2f where name = '%s';", salary,
				name);
		int result = 0;
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			result = statement.executeUpdate(sql);
		} catch (SQLException e) {
			throw new DatabaseException("Unable to update");
		}
		return result;
	}

	public List<Employee> getEmployeeData(String name) throws DatabaseException, SQLException {
		return readData().stream().filter(employee -> employee.name.equals(name)).collect(Collectors.toList());
	}

	/**
	 * updates the second parameter
	 * 
	 * @param name
	 * @param salary
	 * @return
	 * @throws DatabaseException
	 */
	public int updateEmployeeData(String name, double salary) throws DatabaseException {
		return this.updateEmployeeUsingStatement(name, salary);
	}

	/**
	 * Retrieve data according to name
	 * 
	 * @param name
	 * @return
	 * @throws DatabaseException
	 */
	public List<Employee> getEmployeePayrollData(String name) throws DatabaseException {
		List<Employee> employeePayrollList = null;
		if (this.employeeStatement == null)
			this.preparedStatementForEmployeeData();
		try {
			employeeStatement.setString(1, name);
			ResultSet resultSet = employeeStatement.executeQuery();
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private List<Employee> getEmployeePayrollData(ResultSet resultSet) {
		List<Employee> employeePayrollList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				double salary = resultSet.getDouble("salary");
				LocalDate start = resultSet.getDate("start").toLocalDate();
				employeePayrollList.add(new Employee(id, name, salary, start));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private void preparedStatementForEmployeeData() throws DatabaseException {
		try {
			Connection connection = this.getConnection();
			String sql = "SELECT * FROM employee_payroll_service WHERE name = ?";
			employeeStatement = connection.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * UC5, UC10: Implementing query to find employees joined between the particular
	 * dates
	 * 
	 * @param start
	 * @param end
	 * @return
	 * @throws DatabaseException
	 */
	public List<Employee> getEmployeeForDateRange(LocalDate startDate, LocalDate end) throws DatabaseException {
		String sql = String.format(
				"Select id,name,gender,salary,start,department.department_name from employee_payroll_service "
						+ "inner join department on employee_payroll_service.id = department.employee_id where start between '%s' and '%s' ;",
				Date.valueOf(startDate), Date.valueOf(end));
		return this.getEmployeePayrollAndDeparmentData(sql);
	}

	/**
	 * UC10: to work according to new table structure
	 * 
	 * @param sql
	 * @return
	 * @throws DatabaseException
	 */
	private List<Employee> getEmployeePayrollAndDeparmentData(String sql) throws DatabaseException {
		List<Employee> employeePayrollList = new ArrayList<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				double salary = resultSet.getDouble("salary");
				LocalDate start = resultSet.getDate("start").toLocalDate();
				String department = resultSet.getString("department_name");
				employeePayrollList.add(new Employee(id, name, salary, start, department));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private List<Employee> getEmployeePayrollDataUsingDB(String sql) throws DatabaseException {
		List<Employee> employeeData = new ArrayList<>();
		try (Connection connection = this.getConnection();) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			employeeData = this.getEmployeePayrollData(resultSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return employeeData;
	}

	/**
	 * UC6: performing Aggregate functions query on the employee table
	 * 
	 * @param function
	 * @return
	 * @throws DatabaseException
	 */
	public Map<String, Double> getEmployeesByFunction(String function) throws DatabaseException {
		Map<String, Double> aggregateFunctionMap = new HashMap<>();
		String sql = String.format("Select gender, %s(salary) from employee_payroll_service group by gender ; ",
				function);
		try (Connection connection = this.getConnection()) {
			Statement statement = (Statement) connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String gender = resultSet.getString(1);
				Double salary = resultSet.getDouble(2);
				aggregateFunctionMap.put(gender, salary);
			}
		} catch (SQLException exception) {
			throw new DatabaseException("Unable to execute " + function);
		}
		return aggregateFunctionMap;
	}

	/**
	 * UC7: Inserting new employee into the table using JDBC transaction UC8:
	 * Inserting employee data in employee as well as payroll table UC9: Adding the
	 * employee to the given department UC11: Making all insertion as a single
	 * transaction
	 */
	public Employee addEmployeeToPayrollAndDepartment(String name, String gender, double salary, LocalDate start,
			String department) throws SQLException, DatabaseException {
		int employeeId = -1;
		Connection connection = null;
		Employee employee = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try (Statement statement = connection.createStatement()) {
			String sql = String.format("INSERT INTO employee_payroll_service (name, gender, salary, start) "
					+ "VALUES ('%s','%s','%s','%s')", name, gender, salary, Date.valueOf(start));
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
			throw new DatabaseException("Unable to add new employee");
		}
		try (Statement statement = connection.createStatement()) {
			double deductions = salary * 0.2;
			double taxable_pay = salary - deductions;
			double tax = taxable_pay * 0.1;
			double netPay = salary - tax;
			String sql = String.format(
					"INSERT INTO payroll_details (employee_id, basic_pay, deductions, taxable_pay, tax, net_pay) "
							+ "VALUES ('%s','%s','%s','%s','%s','%s')",
					employeeId, salary, deductions, taxable_pay, tax, netPay);
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
			throw new DatabaseException("Unable to add payroll details of  employee");
		}
		try (Statement statement = connection.createStatement()) {
			String sql = String.format(
					"INSERT INTO department (employee_id,department_id, department_name) " + "VALUES ('%s','%s','%s')",
					employeeId, 1, department);
			int rowAffected = statement.executeUpdate(sql);
			if (rowAffected == 1) {
				employee = new Employee(employeeId, name, salary, gender, start, department);
			}
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
			throw new DatabaseException("Unable to add department details of  employee");
		}
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return employee;
	}

	/**
	 * UC8: Performing the cascading delete on the employee table
	 * 
	 * @param name
	 * @throws DatabaseException
	 */
	public void deleteEmployee(String name) throws DatabaseException {
		String sql = String.format("DELETE from employee_payroll_service where name = '%s';", name);
		try {
			Connection connection = this.getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate(sql);
		} catch (SQLException exception) {
			throw new DatabaseException("Unable to delete data");
		}
	}
}

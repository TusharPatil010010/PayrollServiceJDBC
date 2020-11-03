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

	public List<Employee> readData() throws SQLException, DatabaseException {
		String sql = "Select * from payroll_service; ";
		return this.getEmployeePayrollDataUsingDB(sql);
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
	 * UC5 Retrieve employee from given date range
	 * 
	 * @param start
	 * @param end
	 * @return
	 * @throws DatabaseException
	 */
	public List<Employee> getEmployeeForDateRange(LocalDate start, LocalDate end) throws DatabaseException {
		String sql = String.format("Select * from employee_payroll_service where start between '%s' and '%s' ;",
				Date.valueOf(start), Date.valueOf(end));
		return this.getEmployeePayrollDataUsingDB(sql);
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
	 * UC6 returns the aggregate value by given function
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
	 * UC7 add employee to system
	 * 
	 * @param name
	 * @param gender
	 * @param salary
	 * @param start
	 * @return
	 * @throws DatabaseException
	 */
	public Employee addEmployeeToPayroll(String name, String gender, double salary, LocalDate start)
			throws DatabaseException {
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
		return employee;
	}
}

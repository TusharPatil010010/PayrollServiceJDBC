package com.payrollservice;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
		List<Employee> employeeData = new ArrayList<>();
		try (Connection connection = this.getConnection();) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			employeeData = this.getEmployeePayrollData(resultSet);
		} catch (SQLException exception) {
			System.out.println(exception);
		} catch (Exception exception) {
			throw new DatabaseException("Unable to execute query");
		}
		return employeeData;
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

	public int updateEmployeeData(String name, double salary) throws DatabaseException {
		return this.updateEmployeeUsingStatement(name, salary);
	}

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
}

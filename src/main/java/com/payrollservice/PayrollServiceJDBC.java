package com.payrollservice;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PayrollServiceJDBC {
	private Connection getConnection() throws SQLException {
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

	public List<Employee> readData() throws SQLException {
		String sql = "Select * from payroll_service; ";
		List<Employee> employeeData = new ArrayList<>();
		try (Connection connection = this.getConnection();) {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");
				double salary = result.getDouble("salary");
				LocalDate start = result.getDate("start").toLocalDate();
				employeeData.add(new Employee(id, name, salary, start));
			}
		} catch (SQLException exception) {
			System.out.println(exception);
		} catch (Exception exception) {
			throw new SQLException("Unable to execute query");
		}
		return employeeData;
	}
}

package com.capg.payrollservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class EmployeeFileService {
	public static String PAYROLL_FILE_NAME = "payroll.txt";

	public void writeData(List<Employee> list) {
		StringBuffer employeeBuffer = new StringBuffer();
		list.forEach(employee -> {
			String empString = employee.toString().concat("\n");
			employeeBuffer.append(empString);
		});

		try {
			Files.write(Paths.get(PAYROLL_FILE_NAME), employeeBuffer.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printData() {
		try {
			Files.lines(new File(PAYROLL_FILE_NAME).toPath()).forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public long countEntries() {
		long entries = 0;
		try {
			entries = Files.lines(new File(PAYROLL_FILE_NAME).toPath()).count();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return entries;
	}

	public List<Employee> readData() {
		List<Employee> list = new ArrayList<Employee>();
		try {
			Files.lines(new File(PAYROLL_FILE_NAME).toPath()).map(line -> line.trim()).forEach(line -> {
				String[] data = line.split("(, )");
				String[] newData = new String[10];
				int index = 0;
				for (String d : data) {
					String[] splitData = d.split("(=)");
					newData[index] = splitData[1];
					index++;
				}
				list.add(new Employee(Integer.parseInt(newData[0]), newData[1], Double.parseDouble(newData[2])));

			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
}

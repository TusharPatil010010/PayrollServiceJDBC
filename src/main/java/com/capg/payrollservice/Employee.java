package com.capg.payrollservice;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class Employee {
	public String name;
	public int id;
	public double salary;
	public String gender;
	public LocalDate start;
	public List<String> department;
	public boolean is_active = true;

	public Employee(int id, String name, double salary) {
		this.name = name;
		this.id = id;
		this.salary = salary;
	}

	public Employee(int id, String name, double salary, LocalDate start) {
		this(id, name, salary);
		this.start = start;
	}

	public Employee(int id, String name, double salary, LocalDate start, String gender) {
		this(id, name, salary, start);
		this.gender = gender;
	}

	public Employee(int id, String name, double salary, String gender, LocalDate start, List<String> department) {
		this(id, name, salary, start, gender);
		this.department = department;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, gender, salary, start);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Employee that = (Employee) o;
		return id == that.id && Double.compare(that.salary, salary) == 0 && name.equals(that.name);
	}

	@Override
	public String toString() {
		return "id=" + id + ", name=" + name + ", Gender = " + gender + " Salary = " + salary + ", Start Date = "
				+ start + ", Status = " + ((is_active) ? "Active" : "Inactive");
	}

}
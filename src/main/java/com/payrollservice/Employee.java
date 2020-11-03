package com.payrollservice;

import java.time.LocalDate;

public class Employee {
	public String name;
	public int id;
	public double salary;
	private LocalDate start;

	public Employee(int id, String name, double salary) {
		super();
		this.name = name;
		this.id = id;
		this.salary = salary;
	}

	public Employee(int id, String name, double salary, LocalDate start) {
		this(id, name, salary);
		this.start = start;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Employee other = (Employee) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "id=" + id + ", name=" + name + ", salary=" + salary;
	}

}
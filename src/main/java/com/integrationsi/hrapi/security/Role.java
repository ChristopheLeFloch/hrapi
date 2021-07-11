package com.integrationsi.hrapi.security;

import com.hraccess.openhr.IHRRole;
import com.integrationsi.hrapi.models.Employee;

public class Role {
	
	private String code;
	private String value;	
	private String label;
	private Category category;
	private Employee employee;
	
	public Role() {
		
	}
	
	
	public Role(IHRRole r) {
		this.code=r.getTemplate();
		this.value=r.getParameter();
		this.label=r.getTemplateLabel();
		com.hraccess.openhr.IHRRole.Category c = r.getCategory();
		switch(c.getValue()) {
		case "SSEMP": this.category=Category.EMPLOYEE;
		break;
		case "SSMNG": this.category=Category.MANAGER;
		break;		
		case "HRREP": this.category=Category.EXPERT;
		break;	
		}
	
	}
	
	
	
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
		

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}


	public Category getCategory() {
		return category;
	}


	public void setCategory(Category category) {
		this.category = category;
	}


	public void setEmployee(Employee employee) {
		this.employee = employee;		
	}


	public Employee getEmployee() {
		return employee;		
	}
	

}

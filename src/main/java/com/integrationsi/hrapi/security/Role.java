package com.integrationsi.hrapi.security;

import com.hraccess.openhr.IHRRole;

public class Role {
	
	private String code;
	private String value;	
	private String label;
	private Category category;
	private String matcle;
	
	public String getMatcle() {
		return matcle;
	}


	public void setMatcle(String matcle) {
		this.matcle = matcle;
	}


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




}

package com.integrationsi.hrapi.application;

import java.util.List;

import com.integrationsi.hrapi.security.Role;



public class ApplicationAccess {
	
	private List<Application> applications;
	private Role role;
	
	
	public ApplicationAccess(List<Application>  applications, Role role) {
		this.applications = applications;
		this.role = role;
	}
	
	public ApplicationAccess() {
	}

	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}

	public List<Application> getApplications() {
		return applications;
	}

	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}



	
	

}

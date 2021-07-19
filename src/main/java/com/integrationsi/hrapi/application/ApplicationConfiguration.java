package com.integrationsi.hrapi.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.integrationsi.hrapi.security.Role;


public class ApplicationConfiguration {
	
	private Map<String, List<Application>> applicationRoleMap;

	public Map<String, List<Application>> getApplicationRoleMap() {
		return applicationRoleMap;
	}

	public void setApplicationRoleMap(Map<String, List<Application>> applicationsRoleMap) {
		this.applicationRoleMap = applicationsRoleMap;
	}
	
	public List<Application> getApplications(Role role) {
		List<Application> apps = this.applicationRoleMap.get(role.getCode());
		if ( apps == null ) {
			apps = new ArrayList<Application>();
		}
		
		return apps;
	}

}

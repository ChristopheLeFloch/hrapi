package com.integrationsi.hrapi.security;


public class UserLogin {
    
    private String user;
    private String password;
    private String roleCode;
	private String roleValue;
    
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoleCode() {
    	return roleCode;
    }
    
    public void setRoleCode(String roleCode) {
    	this.roleCode = roleCode;
    }

	public String getRoleValue() {
		return roleValue;
	}

	public void setRoleValue(String roleValue) {
		this.roleValue = roleValue;
	}

}
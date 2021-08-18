package com.integrationsi.hrapi.security;

public class NoRoleException extends Exception {

    private String user;
    private String category;
    
    private static final long serialVersionUID = 1L;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    

}
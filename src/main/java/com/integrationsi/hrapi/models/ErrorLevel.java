package com.integrationsi.hrapi.models;

public enum ErrorLevel {

    INFO("INFO"),
    WARN("WARN"), 
    ERROR("ERROR");

    private String label;

    ErrorLevel (String label) {
        this.label=label;
    }

    public String toString() {
        return label;
    }
    
}
package com.integrationsi.hrapi.security;

public enum Category {
    MANAGER("Manager"),
    EMPLOYEE("Salarié"),
    EXPERT("Expert");

    String label;
    Category(String value) {
        this.label = value;
    }

    public String toString() {
        return this.label;
    }
}


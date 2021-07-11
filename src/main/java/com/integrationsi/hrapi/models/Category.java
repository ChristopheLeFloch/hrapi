package com.integrationsi.hrapi.models;

  
public enum Category {
            MANAGER("MANAGER"),
            EMPLOYEE("EMPLOYEE"),
            EXPERT("EXPERT");

            String label;
            Category(String value) {
                this.label = value;
            }

            public String toString() {
                return this.label;
            }
}
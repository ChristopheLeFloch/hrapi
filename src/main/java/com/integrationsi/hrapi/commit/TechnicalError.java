package com.integrationsi.hrapi.commit;

public enum TechnicalError {
	UNKNOWN("01", "Erreur technique", "Erreur Technique");
    String code;
    String label;
    String description;
    
	TechnicalError(String code, String label, String description) {
		this.code = code;
        this.label = label;
        this.description = description;
    }


}

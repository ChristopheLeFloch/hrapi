package com.integrationsi.hrapi.commit;

public enum TechnicalError {
	UNKNOWN("01", "Erreur technique", "Erreur Technique"),
	INIT_COLLECTION("02", "Initialisation des dossiers", "L'initialisation des dossiers n'a pas pu se faire correctement."),
	LOAD_COLLECTION("03", "Chargement des dossiers", "Le chargement des dossiers n'a pas pu se faire correctement."),	
	COMMIT_COLLECTION("04", "Commit des dossiers", "Le commit des dossiers n'a pas pu se faire correctement."),
	CREATE_OCCUR("04", "Cr�ation occurrence", "La cr�ation d'une occurrence est en erreur."),
	UNKNOWN_OCCUR("04", "L'occurrence n'existe plus", "Une occurrence � mettre � jour n'existe plus."),
	BAD_DATA_FORMAT("05", "Probl�me format de donn�es", "Les donn�es mise � jour ne sont pas au bon format."),
	DELETE_ERROR("06", "Probl�me lors de la suppression de donn�es", "Des donn�es ne peuvent pas �tre suprrim�es.");	
	
    private String code;
    private String label;
    private String description;
    
	TechnicalError(String code, String label, String description) {
		this.code = code;
        this.label = label;
        this.description = description;
    }

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


}

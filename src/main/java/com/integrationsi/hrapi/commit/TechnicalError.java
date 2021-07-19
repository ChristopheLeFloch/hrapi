package com.integrationsi.hrapi.commit;

public enum TechnicalError {
	UNKNOWN("01", "Erreur technique", "Erreur Technique"),
	INIT_COLLECTION("02", "Initialisation des dossiers", "L'initialisation des dossiers n'a pas pu se faire correctement."),
	LOAD_COLLECTION("03", "Chargement des dossiers", "Le chargement des dossiers n'a pas pu se faire correctement."),	
	COMMIT_COLLECTION("04", "Commit des dossiers", "Le commit des dossiers n'a pas pu se faire correctement."),
	CREATE_OCCUR("04", "Création occurrence", "La création d'une occurrence est en erreur."),
	UNKNOWN_OCCUR("04", "L'occurrence n'existe plus", "Une occurrence à mettre à jour n'existe plus."),
	BAD_DATA_FORMAT("05", "Problème format de données", "Les données mise à jour ne sont pas au bon format."),
	DELETE_ERROR("06", "Problème lors de la suppression de données", "Des données ne peuvent pas être suprrimées.");	
	
    String code;
    String label;
    String description;
    
	TechnicalError(String code, String label, String description) {
		this.code = code;
        this.label = label;
        this.description = description;
    }


}

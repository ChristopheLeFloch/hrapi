package com.integrationsi.hrapi.commit;

import com.hraccess.openhr.dossier.HRDossier;
import com.hraccess.openhr.msg.HRResultUserError.Error;

public class BusinessCommitError {

	private HRDossier dossier;
	private Error error;
	
	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public BusinessCommitError(HRDossier d, Error error) {
		this.dossier = d;
		this.error = error; 
		
	}
	
	public HRDossier getDossier() {
		return dossier;
	}
	public void setDossier(HRDossier dossier) {
		this.dossier = dossier;
	}
	

}

package com.integrationsi.hrapi.models;

import com.hraccess.openhr.dossier.HRDossier;
import com.hraccess.openhr.msg.HRResultUserError.Error;

public class CommitError {
	private HRDossier dossier;
	private CommitStatus status;
	private Error error;
	
	public CommitError(HRDossier d, Error error) {
		this.dossier = d;
		this.error = error; 
		
	}
	
	public CommitStatus getStatus() {
		return status;
	}
	public void setStatus(CommitStatus status) {
		this.status = status;
	}
	public HRDossier getDossier() {
		return dossier;
	}
	public void setDossier(HRDossier dossier) {
		this.dossier = dossier;
	}

}

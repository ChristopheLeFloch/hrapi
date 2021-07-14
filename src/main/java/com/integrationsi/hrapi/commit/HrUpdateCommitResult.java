package com.integrationsi.hrapi.commit;

import java.util.ArrayList;
import java.util.List;

import com.hraccess.openhr.dossier.HRDossier;
import com.hraccess.openhr.msg.HRResultUserError.Error;

public class HrUpdateCommitResult {
	
	/**
	 * Global status for commit
	 */
	private CommitStatus status;
	public CommitStatus getStatus() {
		return status;
	}
	public void setStatus(CommitStatus status) {
		this.status = status;
	}
	
	/**
	 * Business Errors list returned by commit
	 */
	private List<BusinessCommitError> businessErrors;
	public List<BusinessCommitError> getBusinessErrors() {
		return businessErrors;
	}
	public void setBusinessErrors(List<BusinessCommitError> errors) {
		this.businessErrors = errors;
	}
	

	/**
	 * Liste des erreurs  metier retournees par le commit
	 */
	private List<TechnicalError> technicalErrors;
	public List<TechnicalError> getTechnicalErrors() {
		return technicalErrors;
	}
	public void setTechnicalErrors(List<TechnicalError> errors) {
		this.technicalErrors = errors;
	}
	
	
	public void addTechnicalError(TechnicalError e) {
		if (this.technicalErrors == null) this.technicalErrors = new ArrayList<TechnicalError>();
		this.technicalErrors.add(e);
		
	}
	public void addBusinessError(HRDossier d, Error e) {
		if (this.businessErrors == null) this.businessErrors = new ArrayList<BusinessCommitError>();
		this.businessErrors.add(new BusinessCommitError(d, e));
	}
	
	
}

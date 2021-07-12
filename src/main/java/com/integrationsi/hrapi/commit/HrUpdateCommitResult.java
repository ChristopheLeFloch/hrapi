package com.integrationsi.hrapi.commit;

import java.util.List;

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
	private List<TechnicalCommitError> technicalErrors;
	public List<TechnicalCommitError> getTechnicalErrors() {
		return technicalErrors;
	}
	public void setTechnicalErrors(List<TechnicalCommitError> errors) {
		this.technicalErrors = errors;
	}
	
	
}

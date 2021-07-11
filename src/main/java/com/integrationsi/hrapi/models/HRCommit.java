package com.integrationsi.hrapi.models;

import java.util.ArrayList;
import java.util.List;

import com.hraccess.openhr.dossier.HRDossier;
import com.hraccess.openhr.dossier.HRDossierCollection;
import com.hraccess.openhr.dossier.HRDossierCollection.CommitResult;
import com.hraccess.openhr.dossier.HRDossierCollectionCommitException;
import com.hraccess.openhr.dossier.ICommitResult;
import com.hraccess.openhr.dossier.IHRKey;

public class HRCommit {
	
	private CommitStatus status;
	private String label;
	
	private HRDossierCollection collection;
	
	public HRCommit(HRDossierCollection collection) {
		this.collection = collection;
	}
	public CommitStatus getStatus() {
		return status;
	}
	public String getLabel() {
		return label;
	}
	private List<CommitError> dossierList;
	public List<CommitError> getErrorList() {
		return dossierList;
	}

	
	public void send() {
		this.status = CommitStatus.OK;
		try {
			dossierList = new ArrayList<CommitError>();
			CommitResult result = collection.commitAllDossiers().getDossierCommitResult();
			
			for (com.hraccess.openhr.msg.HRResultUserError.Error error : result.getErrors()) {
                if (error.weight == 5) {                	
                	IHRKey key = result.getErrorDossierId(error).getDossierKey();
                	HRDossier d = collection.getDossier(key);
                	dossierList.add(new CommitError(d,error));  
                	this.status = CommitStatus.HR_ERRORS;
                }
			}
			
		} catch (HRDossierCollectionCommitException e) {
				this.status = CommitStatus.KO;
				this.label = e.toString();

		}
		
		
	}
	
	

}

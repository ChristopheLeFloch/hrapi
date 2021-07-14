package com.integrationsi.hrapi.commit;

public class TechnicalCommitError {
	public TechnicalCommitError(TechnicalError error, String content) {
		super();
		this.error = error;
		this.content = content;
	}
	TechnicalError error;
	String content;
	

}

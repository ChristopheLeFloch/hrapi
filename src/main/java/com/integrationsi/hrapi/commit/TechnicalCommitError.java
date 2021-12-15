package com.integrationsi.hrapi.commit;

public class TechnicalCommitError {
	
	public TechnicalCommitError() {
		
	}

			
	public TechnicalCommitError(TechnicalError error, String content) {
		super();
		this.error = error.toString();
		this.content = content;
	}
	private String error;
	private String content;
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	

}

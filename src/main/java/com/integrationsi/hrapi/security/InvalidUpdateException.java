package com.integrationsi.hrapi.security;

import com.integrationsi.hrapi.commit.TechnicalError;

public class InvalidUpdateException extends Exception {

	public InvalidUpdateException(TechnicalError error) {
		super(error.toString());
	}

	public InvalidUpdateException(TechnicalError error, String message) {
		super (error.toString() + message);
	}

    

}
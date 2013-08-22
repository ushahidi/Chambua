package com.ushahidi.chambua.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * When thrown, this exception results in 400 HTTP status
 * being sent to the client
 *  
 * @author ekala
 *
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
	
	private static final long serialVersionUID = -4510861854793044833L;
	
	public BadRequestException() {
		
	}

	public BadRequestException(String message) {
		super(message);
	}
	
	public BadRequestException(String message, Throwable cause) {
		super(message, cause);
	}

}

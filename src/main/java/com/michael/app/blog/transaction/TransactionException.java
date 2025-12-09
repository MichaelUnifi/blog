package com.michael.app.blog.transaction;

public class TransactionException extends RuntimeException {
	
	private static final long serialVersionUID = -2797474656025884447L;

	public TransactionException(String message, Throwable cause) {
		super(message, cause);
	}
}
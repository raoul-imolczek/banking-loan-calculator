package com.imolczek.school.banking.loan.calculator;

public class LoanCalculationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1001946008372740388L;

	/**
	 * The reason of the exception
	 */
	private String message;
	
	/**
	 * @param message the message to set
	 */
	public LoanCalculationException(String message) {
		this.message = message;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	
}

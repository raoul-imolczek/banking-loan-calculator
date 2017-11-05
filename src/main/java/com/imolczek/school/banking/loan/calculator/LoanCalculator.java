package com.imolczek.school.banking.loan.calculator;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.imolczek.school.banking.loan.calculator.exceptions.LoanCalculationException;
import com.imolczek.school.banking.loan.calculator.exceptions.LoanSettingsException;

/**
 * @author Fabian Bouché
 * A Loan calculator.
 * This calculator only works with monthly installments.
 */
public abstract class LoanCalculator {

	/**
	 * Annual rate of the loan
	 * Example: 5% must be provided as 0.05
	 */
	protected BigDecimal annualRate;
	
	/**
	 * Number of installments
	 */
	protected Integer numberOfInstallments;
	
	/**
	 * The amount to be borrowed
	 */
	protected BigDecimal loanAmount;
	
	/**
	 * The desired remaining balance at the end of the loan
	 */
	protected BigDecimal balloon = BigDecimal.ZERO;
	
	/**
	 * The desired monthly installment
	 */
	protected BigDecimal monthlyInstallment;

	/**
	 * The LocalDate when the loan starts
	 */
	protected LocalDate loanStartDate;
	
	/**
	 * The LocalDate when the first reimbursement will occur
	 * The day of the month must be between 1 and 28 as all recurring installments will happen on the same day
	 */
	protected LocalDate firstReimbursementDate;
	
	public LoanCalculationResult calculateForFixedInstallment() throws LoanSettingsException, LoanCalculationException {
		// Generic input data validation
		validateInputData();
		
		// Specific controls		
		if(monthlyInstallment == null) {
			throw new LoanSettingsException("The desired monthly installment must be set");
		}
		
		// Do the calculation according to the selected implementation (daily interests, lombard year...)
		return doCalculateForFixedInstallment();
	}
		
	/**
	 * Do the calculation according to the selected implementation (daily interests, lombard year...)
	 * @return
	 * @throws LoanCalculationException
	 */
	protected abstract LoanCalculationResult doCalculateForFixedInstallment() throws LoanCalculationException;

	/**
	 * Determines the date of the next reimbursement
	 * @param currentDate
	 * @return
	 */
	protected LocalDate getNextReimbursementDate(LocalDate currentDate) {
		
		int day;
		int month;
		int year;

		if(currentDate.isBefore(firstReimbursementDate)) {
			day = firstReimbursementDate.getDayOfMonth();
			month = firstReimbursementDate.getMonthValue();
			year = firstReimbursementDate.getYear();
		
		} else {
			day = firstReimbursementDate.getDayOfMonth();
			month = currentDate.getMonthValue();
			year = currentDate.getYear();
			month++;
			if(month == 13) {
				month = 1;
				year++;
			}
		}
		
		LocalDate response = LocalDate.of(year, month, day);
		
		return response;
	}
	
	/**
	 * Validate the input data for all calculation cases
	 * @throws LoanSettingsException
	 */
	protected void validateInputData() throws LoanSettingsException {
		if(annualRate == null) {
			throw new LoanSettingsException("The annual rate must be set");
		}
		if(annualRate.compareTo(BigDecimal.ZERO) < 0) {
			throw new LoanSettingsException("The annual rate must be positive");
		}
		if(loanAmount == null) {
			throw new LoanSettingsException("The loan amount must be set");
		}
		if(loanAmount.compareTo(BigDecimal.ZERO) < 0) {
			throw new LoanSettingsException("The loan amount must be positive");
		}
		if(balloon != null) {
			if(balloon.compareTo(loanAmount) > 0) {
				throw new LoanSettingsException("The balloon must be lower than the loan amount");
			}
		}
		if(loanStartDate == null) {
			throw new LoanSettingsException("The loan start LocalDate must be set");
		}
		if(firstReimbursementDate == null) {
			throw new LoanSettingsException("The first reimbursement LocalDate must be set");
		}
		if(firstReimbursementDate.isBefore(loanStartDate)) {
			throw new LoanSettingsException("The first reimbursement LocalDate must be after the loan start LocalDate");
		}
		if(new Integer(firstReimbursementDate.getDayOfMonth()) > 28) {
			throw new LoanSettingsException("The day of the month for the first reimbursement must be between 1 and 28");
		}
		
		if(numberOfInstallments != null) {
			if(numberOfInstallments <= 0) {
				throw new LoanSettingsException("The number of installments must be at least 1");
			}
		}

		if(monthlyInstallment != null) {
			if(monthlyInstallment.compareTo(BigDecimal.ZERO) <= 0) {
				throw new LoanSettingsException("The monthly installments must be positive and cannot be zero");
			}
		}

	}
	
	/**
	 * @return the annualRate
	 */
	public BigDecimal getAnnualRate() {
		return annualRate;
	}

	/**
	 * @param annualRate the annualRate to set
	 */
	public void setAnnualRate(BigDecimal annualRate) {
		this.annualRate = annualRate;
	}

	/**
	 * @return the numberOfInstallments
	 */
	public Integer getNumberOfInstallments() {
		return numberOfInstallments;
	}

	/**
	 * @param numberOfInstallments the numberOfInstallments to set
	 */
	public void setNumberOfInstallments(Integer numberOfInstallments) {
		this.numberOfInstallments = numberOfInstallments;
	}

	/**
	 * @return the loanAmount
	 */
	public BigDecimal getLoanAmount() {
		return loanAmount;
	}

	/**
	 * @param loanAmount the loanAmount to set
	 */
	public void setLoanAmount(BigDecimal loanAmount) {
		this.loanAmount = loanAmount;
	}

	/**
	 * @return the balloon
	 */
	public BigDecimal getBalloon() {
		return balloon;
	}

	/**
	 * @param balloon the balloon to set
	 */
	public void setBalloon(BigDecimal balloon) {
		this.balloon = balloon;
	}

	/**
	 * @return the monthlyInstallment
	 */
	public BigDecimal getMonthlyInstallment() {
		return monthlyInstallment;
	}

	/**
	 * @param monthlyInstallment the monthlyInstallment to set
	 */
	public void setMonthlyInstallment(BigDecimal monthlyInstallment) {
		this.monthlyInstallment = monthlyInstallment;
	}


	/**
	 * @return the loanStartDate
	 */
	public LocalDate getLoanStartDate() {
		return loanStartDate;
	}


	/**
	 * @param loanStartDate the loanStartDate to set
	 */
	public void setLoanStartDate(LocalDate loanStartDate) {
		this.loanStartDate = loanStartDate;
	}


	/**
	 * @return the firstReimbursementDate
	 */
	public LocalDate getFirstReimbursementDate() {
		return firstReimbursementDate;
	}


	/**
	 * @param firstReimbursementDate the firstReimbursementDate to set
	 */
	public void setFirstReimbursementDate(LocalDate firstReimbursementDate) {
		this.firstReimbursementDate = firstReimbursementDate;
	}
	
}

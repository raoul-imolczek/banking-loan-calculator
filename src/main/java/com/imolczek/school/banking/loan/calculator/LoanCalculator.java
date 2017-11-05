package com.imolczek.school.banking.loan.calculator;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

/**
 * A Loan calculator.
 * This calculator only works with monthly installments.
 * @author Fabian Bouché
 *
 */
public class LoanCalculator {

	/**
	 * With Lombard calculation, we consider that all months are 30 days long
	 * Otherwise, we count the exact number of days between installments for interest calculation
	 */
	private boolean lombardCalculation;
	
	/**
	 * Annual rate of the loan
	 * Example: 5% must be provided as 0.05
	 */
	private BigDecimal annualRate;
	
	/**
	 * Number of installments
	 */
	private Integer numberOfInstallments;
	
	/**
	 * The amount to be borrowed
	 */
	private BigDecimal loanAmount;
	
	/**
	 * The desired remaining balance at the end of the loan
	 */
	private BigDecimal balloon;
	
	/**
	 * The desired monthly installment
	 */
	private BigDecimal monthlyInstallment;

	/**
	 * The LocalDate when the loan starts
	 */
	private LocalDate loanStartDate;
	
	/**
	 * The LocalDate when the first reimbursement will occur
	 * The day of the month must be between 1 and 28 as all recurring installments will happen on the same day
	 */
	private LocalDate firstReimbursementDate;
	
	public LoanCalculationResult calculateForFixedInstallment() throws LoanSettingsException, LoanCalculationException {
		// Generic input data validation
		validateInputData();
		
		// Specific controls		
		if(monthlyInstallment == null) {
			throw new LoanSettingsException("The desired monthly installment must be set");
		}
		
		LoanCalculationResult result = new LoanCalculationResult();
		
		BigDecimal remainingBalance = loanAmount;
		LocalDate currentDate = loanStartDate;
		boolean firstIteration = true;
		while(remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
			LocalDate nextDate = getNextReimbursementDate(currentDate);
			long daysOfInterest365 = getNumberOfDays365BetweenDates(currentDate, nextDate);
			long daysOfInterest366 = getNumberOfDays366BetweenDates(currentDate, nextDate);
			
			remainingBalance = remainingBalance.add(getStandardMonthlyInterest(remainingBalance, daysOfInterest365, daysOfInterest366));
			
			if (monthlyInstallment.compareTo(remainingBalance) < 0) {
				remainingBalance = remainingBalance.subtract(monthlyInstallment);
			} else {
				remainingBalance = BigDecimal.ZERO;
			}
			firstIteration = false;
		}
		
		return result;
	}
	
	protected BigDecimal getStandardMonthlyInterest(BigDecimal remainingBalance, long daysOfInterest365, long daysOfInterest366) {
		BigDecimal days365 = new BigDecimal(daysOfInterest365);
		BigDecimal days366 = new BigDecimal(daysOfInterest366);
		BigDecimal big365 = new BigDecimal(365);
		BigDecimal big366 = new BigDecimal(366);
		BigDecimal monthlyInterest = remainingBalance.multiply(BigDecimal.ONE.add(days365.multiply(annualRate.divide(big365)).add(days366.multiply(annualRate.divide(big366)))));
		return monthlyInterest;
	}
	
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
	
	protected long getNumberOfDays365BetweenDates(LocalDate start, LocalDate end) throws LoanCalculationException {
		if (start.isAfter(end)) {
			throw new LoanCalculationException("Start cannot be after end");
		}
		if (end.getYear() - start.getYear() > 1) {
			throw new LoanCalculationException("Case of deferment bigger than 1 year not covered");
		}
		long days;
		if (start.isLeapYear()) {
			if (end.isLeapYear()) {
				days = 0;
			} else {
				LocalDate startOfNonLeapYear = LocalDate.of(end.getYear(), 1, 1);
				days = ChronoUnit.DAYS.between(startOfNonLeapYear, end);
			}
		} else if (end.isLeapYear()) {
			LocalDate endOfNonLeapYear = LocalDate.of(start.getYear(), 12, 31);
			days = ChronoUnit.DAYS.between(start, endOfNonLeapYear);
		} else {
			days = ChronoUnit.DAYS.between(start, end);
		}
		return days;
	}

	protected long getNumberOfDays366BetweenDates(LocalDate start, LocalDate end) throws LoanCalculationException {
		if (start.isAfter(end)) {
			throw new LoanCalculationException("Start cannot be after end");
		}
		if (end.getYear() - start.getYear() > 1) {
			throw new LoanCalculationException("Case of deferment bigger than 1 year not covered");
		}
		long days;
		if (start.isLeapYear()) {
			if (end.isLeapYear()) {
				days = ChronoUnit.DAYS.between(start, end);
			} else {
				LocalDate endOfLeapYear = LocalDate.of(start.getYear(), 12, 31);
				days = ChronoUnit.DAYS.between(start, endOfLeapYear);
			}
		} else if (end.isLeapYear()) {
			LocalDate startOfLeapYear = LocalDate.of(end.getYear(), 1, 1);
			days = ChronoUnit.DAYS.between(startOfLeapYear, end);
		} else {
			days = 0;
		}
		return days;
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

	/**
	 * @return the lombardCalculation
	 */
	public boolean isLombardCalculation() {
		return lombardCalculation;
	}

	/**
	 * @param lombardCalculation the lombardCalculation to set
	 */
	public void setLombardCalculation(boolean lombardCalculation) {
		this.lombardCalculation = lombardCalculation;
	}
	
	
}

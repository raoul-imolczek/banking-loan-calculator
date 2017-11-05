package com.imolczek.school.banking.loan.calculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.imolczek.school.banking.loan.calculator.exceptions.LoanCalculationException;
import com.imolczek.school.banking.loan.model.CashStream;

/**
 * @author Fabian Bouché
 * This calculator applies interests on a daily basis
 */
public class StandardLoanCalculator extends LoanCalculator {

	@Override
	protected LoanCalculationResult doCalculateForFixedInstallment() throws LoanCalculationException {
		LoanCalculationResult result = new LoanCalculationResult();
		
		BigDecimal remainingBalance = loanAmount;
		LocalDate currentDate = loanStartDate;

		CashStream initialCashStream = new CashStream();
		initialCashStream.setAmount(loanAmount.negate());
		initialCashStream.setDate(loanStartDate);
		
		result.getAmortizationSchedule().getCashStreamList().add(initialCashStream );
		
		while(remainingBalance.compareTo(balloon) > 0) {
			LocalDate nextDate = getNextReimbursementDate(currentDate);
			long daysOfInterest365 = getNumberOfDays365BetweenDates(currentDate, nextDate);
			long daysOfInterest366 = getNumberOfDays366BetweenDates(currentDate, nextDate);
			
			remainingBalance = remainingBalance.add(getInterests(remainingBalance, daysOfInterest365, daysOfInterest366));
			
			CashStream cashStream = new CashStream();
			cashStream.setDate(currentDate);

			if (monthlyInstallment.compareTo(remainingBalance.subtract(balloon)) < 0) {
				remainingBalance = remainingBalance.subtract(monthlyInstallment);
				cashStream.setAmount(monthlyInstallment);
			} else {
				remainingBalance = balloon;
				cashStream.setAmount(remainingBalance.subtract(balloon));
			}
			
			result.getAmortizationSchedule().getCashStreamList().add(cashStream);
		}
		
		result.calculateAPR();
		
		return result;
	}	

	/**
	 * Calculates the interests for the installment
	 * Beware that the daily interest rate has a different value for a leap year and that some installments may cover a standard and a leap year
	 * @param remainingBalance Current remaining balance
	 * @param daysOfInterest365 Number of days in a standard year the interests have to be calculated for
	 * @param daysOfInterest366 Number of days in a leap year the interests have to be calculated for
	 * @return The amount of interests
	 */
	protected BigDecimal getInterests(BigDecimal remainingBalance, long daysOfInterest365, long daysOfInterest366) {
		BigDecimal days365 = new BigDecimal(daysOfInterest365);
		BigDecimal days366 = new BigDecimal(daysOfInterest366);
		BigDecimal big365 = new BigDecimal(365);
		BigDecimal big366 = new BigDecimal(366);
		BigDecimal interests = remainingBalance.multiply(BigDecimal.ONE.add(days365.multiply(annualRate.divide(big365)).add(days366.multiply(annualRate.divide(big366)))));
		return interests;
	}
	
	/**
	 * Determine the number of days in a standard year between both days
	 * @param start
	 * @param end
	 * @return
	 * @throws LoanCalculationException
	 */
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

	/**
	 * Determine the number of days in a leap year between both days
	 * @param start
	 * @param end
	 * @return
	 * @throws LoanCalculationException
	 */
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
	
}

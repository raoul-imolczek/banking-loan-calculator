package com.imolczek.school.banking.loan.calculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.imolczek.school.banking.loan.calculator.dateutils.LoanDateUtil;
import com.imolczek.school.banking.loan.calculator.exceptions.LoanCalculationException;
import com.imolczek.school.banking.loan.model.CashStream;

/**
 * @author Fabian Bouché
 * This calculator applies interests on a daily basis
 */
public class StandardLoanCalculator extends LoanCalculator {

	private LoanDateUtil dateUtil = new LoanDateUtil();
	
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
			long daysOfInterest365 = dateUtil.getNumberOfDays365BetweenDates(currentDate, nextDate);
			long daysOfInterest366 = dateUtil.getNumberOfDays366BetweenDates(currentDate, nextDate);
			
			BigDecimal interest = getInterests(remainingBalance, daysOfInterest365, daysOfInterest366);
			BigDecimal fees = BigDecimal.ZERO;
			BigDecimal insuranceCost = BigDecimal.ZERO;
			
			remainingBalance = remainingBalance.add(interest);
			remainingBalance = remainingBalance.add(insuranceCost);
			remainingBalance = remainingBalance.add(fees);
			
			CashStream cashStream = new CashStream();
			cashStream.setDate(currentDate);
			cashStream.setInsuranceCost(insuranceCost);
			cashStream.setFees(fees);
			cashStream.setInterest(interest);

			BigDecimal principalRepaymentAmount;
			if (monthlyInstallment.compareTo(remainingBalance.subtract(balloon)) < 0) {
				remainingBalance = remainingBalance.subtract(monthlyInstallment);
				cashStream.setAmount(monthlyInstallment);
				principalRepaymentAmount = monthlyInstallment.subtract(insuranceCost).subtract(interest).subtract(fees);
			} else {
				remainingBalance = balloon;
				cashStream.setAmount(remainingBalance.subtract(balloon));
				principalRepaymentAmount = remainingBalance.subtract(balloon).subtract(insuranceCost).subtract(interest).subtract(fees);
			}

			cashStream.setPrincipalRepaymentAmount(principalRepaymentAmount);

			result.getAmortizationSchedule().getCashStreamList().add(cashStream);
		}
		
		result.calculateAPR();
		result.calculateTotalInterest();
		
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
		BigDecimal interests = remainingBalance.multiply(BigDecimal.ONE.add(days365.multiply(annualRate.divide(big365)).add(days366.multiply(annualRate.divide(big366))))).setScale(2, BigDecimal.ROUND_HALF_UP);
		return interests;
	}
	
	
}

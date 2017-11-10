package com.imolczek.school.banking.loan.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.imolczek.school.banking.loan.calculator.dateutils.LoanDateUtil;
import com.imolczek.school.banking.loan.calculator.exceptions.LoanCalculationException;
import com.imolczek.school.banking.loan.model.CashStream;
import com.imolczek.school.banking.loan.model.LoanCalculationResult;

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
		
		result.getAmortizationSchedule().getCashStreamList().add(initialCashStream);
		
		while(remainingBalance.compareTo(balloon) > 0) {
			LocalDate nextDate = getNextReimbursementDate(currentDate);
			long daysOfInterest365 = dateUtil.getNumberOfDays365BetweenDates(currentDate, nextDate);
			long daysOfInterest366 = dateUtil.getNumberOfDays366BetweenDates(currentDate, nextDate);
			currentDate = nextDate;
			
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
				cashStream.setAmount(remainingBalance.subtract(balloon));
				principalRepaymentAmount = remainingBalance.subtract(balloon).subtract(insuranceCost).subtract(interest).subtract(fees);
				remainingBalance = balloon;
			}

			cashStream.setPrincipalRepaymentAmount(principalRepaymentAmount);

			result.getAmortizationSchedule().getCashStreamList().add(cashStream);
		}
		
		APRCalculator aprCalculator = new APRCalculator();
		aprCalculator.calculateAPR(result);
		
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
		BigDecimal interests = remainingBalance.multiply((days365.multiply(annualRate.divide(big365, 4, RoundingMode.HALF_UP)).add(days366.multiply(annualRate.divide(big366, 4, RoundingMode.HALF_UP))))).setScale(2, BigDecimal.ROUND_HALF_UP);
		return interests;
	}
	
	
}

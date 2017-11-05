package com.imolczek.school.banking.loan.calculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Iterator;

import com.imolczek.school.banking.loan.calculator.dateutils.LoanDateUtil;
import com.imolczek.school.banking.loan.calculator.exceptions.LoanCalculationException;
import com.imolczek.school.banking.loan.model.AmortizationSchedule;
import com.imolczek.school.banking.loan.model.CashStream;

/**
 * @author Fabian Bouché
 * A loan calculation result provides the amortization schedule as well as the APR
 */
public class LoanCalculationResult {

	private LoanDateUtil dateUtil = new LoanDateUtil();
	
	/**
	 * The amortizationSchedule of the loan
	 */
	private AmortizationSchedule amortizationSchedule;
	
	/**
	 * The APR of the loan
	 */
	private BigDecimal apr;
	
	/**
	 * The sum of the interests paid
	 */
	private BigDecimal totalInterestCost;

	/**
	 * @return the amortizationSchedule
	 */
	public AmortizationSchedule getAmortizationSchedule() {
		return amortizationSchedule;
	}

	/**
	 * @param amortizationSchedule the amortizationSchedule to set
	 */
	public void setAmortizationSchedule(AmortizationSchedule amortizationSchedule) {
		this.amortizationSchedule = amortizationSchedule;
	}

	/**
	 * @return the totalInterestCost
	 */
	public BigDecimal getTotalInterestCost() {
		return totalInterestCost;
	}

	/**
	 * @param totalInterestCost the totalInterestCost to set
	 */
	public void setTotalInterestCost(BigDecimal totalInterestCost) {
		this.totalInterestCost = totalInterestCost;
	}

	/**
	 * @return the apr
	 */
	public BigDecimal getApr() {
		return apr;
	}

	/**
	 * @param apr the apr to set
	 */
	public void setApr(BigDecimal apr) {
		this.apr = apr;
	}

	public void calculateAPR() throws LoanCalculationException {
		BigDecimal lowAPR = new BigDecimal("0").setScale(4, BigDecimal.ROUND_HALF_UP);
		BigDecimal highAPR = new BigDecimal("10").setScale(4, BigDecimal.ROUND_HALF_UP);
		
		while (highAPR.compareTo(lowAPR) != 0) {
			boolean lowerAPR = testAPR(lowAPR, highAPR);
			if(lowerAPR) {
				highAPR = lowAPR.add(highAPR.subtract(lowAPR).divide(new BigDecimal("2")));
			} else {
				lowAPR = lowAPR.add(highAPR.subtract(lowAPR).divide(new BigDecimal("2")));
			}
		}
	}

	private boolean testAPR(BigDecimal lowAPR, BigDecimal highAPR) throws LoanCalculationException {
		BigDecimal sum = BigDecimal.ZERO;
		double testAPR = lowAPR.add(highAPR.subtract(lowAPR).divide(new BigDecimal(2))).doubleValue();
		LocalDate start = amortizationSchedule.getCashStreamList().get(0).getDate();
		Iterator<CashStream> iterator = amortizationSchedule.getCashStreamList().iterator();
		while(iterator.hasNext()) {
			CashStream stream = iterator.next();
			BigDecimal divisor = new BigDecimal(Math.pow(1 + testAPR, new Double(dateUtil.getNumberOfDays365BetweenDates(start, stream.getDate())) / 365 + new Double(dateUtil.getNumberOfDays366BetweenDates(start, stream.getDate())) / 366));
			BigDecimal valuatedStream = stream.getAmount().divide(divisor);
			sum.add(valuatedStream);
		}
		if(sum.signum() < 0) {
			return true;
		} else {
			return false;
		}
	}

	public void calculateTotalInterest() {
	}

}

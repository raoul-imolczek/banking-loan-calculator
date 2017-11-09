package com.imolczek.school.banking.loan.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Iterator;

import com.imolczek.school.banking.loan.calculator.dateutils.LoanDateUtil;
import com.imolczek.school.banking.loan.calculator.exceptions.LoanCalculationException;
import com.imolczek.school.banking.loan.calculator.model.LoanCalculationResult;
import com.imolczek.school.banking.loan.model.AmortizationSchedule;
import com.imolczek.school.banking.loan.model.CashStream;

public class APRCalculator {

	private LoanDateUtil dateUtil = new LoanDateUtil();
		
	public void calculateAPR(LoanCalculationResult result) throws LoanCalculationException {
		BigDecimal lowAPR = new BigDecimal("0").setScale(5, BigDecimal.ROUND_HALF_DOWN);
		BigDecimal highAPR = new BigDecimal("10").setScale(5, BigDecimal.ROUND_HALF_DOWN);
		
		while (highAPR.subtract(lowAPR).compareTo(BigDecimal.ONE.movePointRight(-5)) <= 0) {
			System.out.println("L: " + lowAPR + " H: " + highAPR);
			boolean lowerAPR = testAPR(result.getAmortizationSchedule(), lowAPR, highAPR);
			if(lowerAPR) {
				highAPR = lowAPR.add(highAPR.subtract(lowAPR).divide(new BigDecimal("2"), 5, BigDecimal.ROUND_HALF_DOWN));
			} else {
				lowAPR = lowAPR.add(highAPR.subtract(lowAPR).divide(new BigDecimal("2"), 5, BigDecimal.ROUND_HALF_DOWN));
			}
		}
		
		result.setApr(lowAPR.setScale(4, BigDecimal.ROUND_HALF_UP));
	}

	private boolean testAPR(AmortizationSchedule amortizationSchedule, BigDecimal lowAPR, BigDecimal highAPR) throws LoanCalculationException {
		BigDecimal sum = BigDecimal.ZERO.setScale(5, BigDecimal.ROUND_HALF_UP);
		double testAPR = lowAPR.add(highAPR.subtract(lowAPR).divide(new BigDecimal(2), 5, BigDecimal.ROUND_HALF_DOWN)).doubleValue();
		LocalDate start = amortizationSchedule.getCashStreamList().get(0).getDate();
		Iterator<CashStream> iterator = amortizationSchedule.getCashStreamList().iterator();

		int k = new Long(Math.round(Math.log10(amortizationSchedule.getCashStreamList().size()))).intValue();
		
		while(iterator.hasNext()) {
			CashStream stream = iterator.next();
			
			double vRate = 1 + testAPR;
			double days365 = dateUtil.getNumberOfDays365BetweenDates(start, stream.getDate());
			double days366 = dateUtil.getNumberOfDays366BetweenDates(start, stream.getDate());
			double power = days365 / 365d + days366 / 366d;
			
			BigDecimal divisor = new BigDecimal(Math.pow(vRate, power)).setScale(2 + k, BigDecimal.ROUND_HALF_UP);
			BigDecimal valuatedStream = stream.getAmount().divide(divisor, 2 + k, BigDecimal.ROUND_HALF_UP);
			sum = sum.add(valuatedStream);
		}
		if(sum.signum() < 0) {
			return true;
		} else {
			return false;
		}
	}

}

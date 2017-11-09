package com.imolczek.school.banking.loan.calculator.test;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.imolczek.school.banking.loan.calculator.StandardLoanCalculator;
import com.imolczek.school.banking.loan.calculator.model.LoanCalculationResult;

import junit.framework.TestCase;

public class StandardLoanCalculatorTest extends TestCase {

	public void testCalculator() throws Exception {
		StandardLoanCalculator calculator = new StandardLoanCalculator();
		calculator.setAnnualRate(BigDecimal.ZERO);
		calculator.setLoanStartDate(LocalDate.of(2017, 1, 12));
		calculator.setFirstReimbursementDate(LocalDate.of(2017, 2, 7));
		calculator.setLoanAmount(new BigDecimal(10000));
		calculator.setMonthlyInstallment(new BigDecimal(500));
		LoanCalculationResult result = calculator.calculateForFixedInstallment();
		assertEquals(BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP), result.getApr());
		assertEquals(21,  result.getAmortizationSchedule().getCashStreamList().size());
		assertEquals(new BigDecimal(500).setScale(2, BigDecimal.ROUND_HALF_UP), result.getAmortizationSchedule().getCashStreamList().get(20).getAmount());
	}
		
}

package com.imolczek.school.banking.loan.calculator.test;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.imolczek.school.banking.loan.calculator.LoanCalculationResult;
import com.imolczek.school.banking.loan.calculator.StandardLoanCalculator;
import com.imolczek.school.banking.loan.calculator.exceptions.LoanCalculationException;
import com.imolczek.school.banking.loan.calculator.exceptions.LoanSettingsException;

import junit.framework.TestCase;

public class StandardLoanCalculatorTest extends TestCase {

	public void testCalculator() throws Exception {
		StandardLoanCalculator calculator = new StandardLoanCalculator();
		calculator.setAnnualRate(new BigDecimal(0.05));
		calculator.setLoanStartDate(LocalDate.of(2017, 1, 12));
		calculator.setFirstReimbursementDate(LocalDate.of(2017, 2, 7));
		calculator.setLoanAmount(new BigDecimal(10000));
		calculator.setMonthlyInstallment(new BigDecimal(500));
		LoanCalculationResult result = calculator.calculateForFixedInstallment();
	}
		
}

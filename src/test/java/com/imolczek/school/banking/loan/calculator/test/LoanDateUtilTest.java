package com.imolczek.school.banking.loan.calculator.test;

import java.time.LocalDate;

import com.imolczek.school.banking.loan.calculator.dateutils.LoanDateUtil;

import junit.framework.TestCase;

public class LoanDateUtilTest extends TestCase {

	private LoanDateUtil dateUtil = new LoanDateUtil();
	
	public void testLoanDateUtil() throws Exception {
		assertEquals(dateUtil.getNumberOfDays365BetweenDates(LocalDate.of(2017, 12, 1), LocalDate.of(2017, 12, 31)), 30);
		assertEquals(dateUtil.getNumberOfDays366BetweenDates(LocalDate.of(2017, 12, 1), LocalDate.of(2017, 12, 31)), 0);
		assertEquals(dateUtil.getNumberOfDays365BetweenDates(LocalDate.of(2020, 12, 1), LocalDate.of(2020, 12, 31)), 0);
		assertEquals(dateUtil.getNumberOfDays366BetweenDates(LocalDate.of(2020, 12, 1), LocalDate.of(2020, 12, 31)), 30);
		assertEquals(dateUtil.getNumberOfDays365BetweenDates(LocalDate.of(2019, 12, 31), LocalDate.of(2020, 1, 10)), 1);
		assertEquals(dateUtil.getNumberOfDays366BetweenDates(LocalDate.of(2019, 12, 31), LocalDate.of(2020, 1, 10)), 9);
		assertEquals(dateUtil.getNumberOfDays365BetweenDates(LocalDate.of(2020, 12, 31), LocalDate.of(2021, 1, 10)), 9);
		assertEquals(dateUtil.getNumberOfDays366BetweenDates(LocalDate.of(2020, 12, 31), LocalDate.of(2021, 1, 10)), 1);
	}
	
}

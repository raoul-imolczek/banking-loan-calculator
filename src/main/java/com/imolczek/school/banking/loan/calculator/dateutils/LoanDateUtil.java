package com.imolczek.school.banking.loan.calculator.dateutils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.imolczek.school.banking.loan.calculator.exceptions.LoanCalculationException;

public class LoanDateUtil {

	/**
	 * Determine the number of days in a standard year between both days
	 * @param start
	 * @param end
	 * @return
	 * @throws LoanCalculationException
	 */
	public long getNumberOfDays365BetweenDates(LocalDate start, LocalDate end) throws LoanCalculationException {
		if (start.isAfter(end)) {
			throw new LoanCalculationException("Start cannot be after end");
		}
		if (end.getYear() - start.getYear() > 1) {
			long fullYearsDayCount = 0;
			int middleYearBegin = start.getYear() + 1;
			int middleYearEnd = end.getYear() - 1;
			for(int i = middleYearBegin; i <= middleYearEnd; i++) {
				if(!LocalDate.of(i, 1, 1).isLeapYear()) {
					fullYearsDayCount += 365;
				}
			}
			long before = getNumberOfDays365BetweenDates(start, LocalDate.of(start.getYear(), 12, 31));
			long after = getNumberOfDays365BetweenDates(LocalDate.of(end.getYear(), 1, 1), end);
			return before + fullYearsDayCount + after;
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
	public long getNumberOfDays366BetweenDates(LocalDate start, LocalDate end) throws LoanCalculationException {
		if (start.isAfter(end)) {
			throw new LoanCalculationException("Start cannot be after end");
		}
		if (end.getYear() - start.getYear() > 1) {
			long fullYearsDayCount = 0;
			int middleYearBegin = start.getYear() + 1;
			int middleYearEnd = end.getYear() - 1;
			for(int i = middleYearBegin; i <= middleYearEnd; i++) {
				if(LocalDate.of(i, 1, 1).isLeapYear()) {
					fullYearsDayCount += 366;
				}
			}
			long before = getNumberOfDays366BetweenDates(start, LocalDate.of(start.getYear(), 12, 31));
			long after = getNumberOfDays366BetweenDates(LocalDate.of(end.getYear(), 1, 1), end);
			return before + fullYearsDayCount + after;
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

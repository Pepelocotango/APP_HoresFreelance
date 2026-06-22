package com.freelance.hores.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

data class YearMonth(val year: Int, val monthNumber: Int)

fun todayLocalDate(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

fun nowLocalTime(): LocalTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time

fun todayYearMonth(): YearMonth {
    val date = todayLocalDate()
    return YearMonth(date.year, date.monthNumber)
}

fun YearMonth.atDay(day: Int): LocalDate = LocalDate(year, monthNumber, day)

fun YearMonth.atEndOfMonth(): LocalDate = atDay(lengthOfMonth())

fun YearMonth.lengthOfMonth(): Int = atDay(1).lengthOfMonth()

fun YearMonth.plusMonths(months: Long): YearMonth {
    var y = year
    var m = monthNumber + months.toInt()
    while (m > 12) {
        m -= 12
        y++
    }
    while (m < 1) {
        m += 12
        y--
    }
    return YearMonth(y, m)
}

fun YearMonth.minusMonths(months: Long): YearMonth = plusMonths(-months)

fun LocalTime.totalSecondsFromMidnight(): Long =
    hour * 3600L + minute * 60L + second

fun localTimeFromSecondOfDay(seconds: Long): LocalTime {
    val h = (seconds / 3600).toInt()
    val m = ((seconds % 3600) / 60).toInt()
    val s = (seconds % 60).toInt()
    return LocalTime(h, m, s)
}

fun LocalDate.minusDays(days: Long): LocalDate =
    LocalDate.fromEpochDays(toEpochDays() - days.toInt())

fun LocalDate.plusDays(days: Long): LocalDate =
    LocalDate.fromEpochDays(toEpochDays() + days.toInt())

fun LocalDate.withDayOfMonth(day: Int): LocalDate =
    LocalDate(year, monthNumber, day)

fun LocalDate.lengthOfMonth(): Int = when (monthNumber) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    else -> 30
}

fun DayOfWeek.isoDayNumber(): Int = when (this) {
    DayOfWeek.MONDAY -> 1
    DayOfWeek.TUESDAY -> 2
    DayOfWeek.WEDNESDAY -> 3
    DayOfWeek.THURSDAY -> 4
    DayOfWeek.FRIDAY -> 5
    DayOfWeek.SATURDAY -> 6
    DayOfWeek.SUNDAY -> 7
}

fun LocalDate.isoDayOfWeek(): Int = dayOfWeek.isoDayNumber()

fun epochMillisToLocalDate(millis: Long): LocalDate =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date

fun localDateToEpochMillis(date: LocalDate): Long =
    date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

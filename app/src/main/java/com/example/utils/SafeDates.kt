@file:Suppress("unused")
package com.example.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Thread-safe date formatting utilities using java.time.
 *
 * Replaces the legacy SimpleDateFormat-based approach which is:
 * - NOT thread-safe (shared formatters can corrupt in concurrent usage)
 * - Locale-sensitive (can break on non-English locales)
 * - Error-prone (no safe parsing)
 *
 * All methods use ISO-8601 internally (yyyy-MM-dd) and format to
 * display format (dd/MM/yyyy) only at the presentation boundary.
 */
object SafeDates {

    private val ISO = DateTimeFormatter.ISO_LOCAL_DATE             // yyyy-MM-dd
    private val DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy") // dd/MM/yyyy
    private val FILE = DateTimeFormatter.ofPattern("dd-MM-yyyy")    // dd-MM-yyyy

    // ──── Factory ────

    /** Today in ISO format: "2026-09-27" */
    fun todayIso(): String = LocalDate.now().format(ISO)

    /** Today + N days in ISO format */
    fun todayPlusDaysIso(days: Long): String = LocalDate.now().plusDays(days).format(ISO)

    // ──── Parsing ────

    /** Parse ISO date string safely. Returns null on invalid input. */
    fun parseIso(isoDate: String): LocalDate? =
        runCatching { LocalDate.parse(isoDate, ISO) }.getOrNull()

    /** Parse display format (dd/MM/yyyy) safely. Returns null on invalid input. */
    fun parseDisplay(displayDate: String): LocalDate? =
        runCatching { LocalDate.parse(displayDate, DISPLAY) }.getOrNull()

    // ──── Formatting ────

    /** ISO → display format: "2026-09-27" → "27/09/2026" */
    fun isoToDisplay(isoDate: String): String? =
        parseIso(isoDate)?.format(DISPLAY)

    /** ISO → file-safe format: "2026-09-27" → "27-09-2026" */
    fun isoToFile(isoDate: String): String? =
        parseIso(isoDate)?.format(FILE)

    /** Display → ISO format: "27/09/2026" → "2026-09-27" */
    fun displayToIso(displayDate: String): String? =
        parseDisplay(displayDate)?.format(ISO)

    // ──── Validation ────

    /** Check if a string is a valid ISO date */
    fun isValidIso(date: String): Boolean = parseIso(date) != null

    /** Check if a string is a valid display-format date */
    fun isValidDisplay(date: String): Boolean = parseDisplay(date) != null

    // ──── Comparison ────

    /** Check if isoDate is today or before today */
    fun isPastOrToday(isoDate: String): Boolean =
        parseIso(isoDate)?.let { !it.isAfter(LocalDate.now()) } ?: false

    /** Check if isoDate is overdue (past today) */
    fun isOverdue(isoDate: String): Boolean =
        parseIso(isoDate)?.let { it.isBefore(LocalDate.now()) } ?: false

    /** Days between today and isoDate (negative = past) */
    fun daysFromToday(isoDate: String): Long? =
        parseIso(isoDate)?.let { java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), it) }
}

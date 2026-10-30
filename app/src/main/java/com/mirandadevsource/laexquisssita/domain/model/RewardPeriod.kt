package com.mirandadevsource.laexquisssita.domain.model

import java.time.LocalDate
import java.time.ZoneId

/** Calendar semester used to determine reward redemption eligibility. */
data class RewardPeriod(
    val startInclusive: LocalDate,
    val endExclusive: LocalDate,
) {
    val label: String
        get() = "Periodo ${if (startInclusive.monthValue == 1) "Ene - Jun" else "Jul - Dic"} ${startInclusive.year}"

    fun startTimestamp(zone: ZoneId): String = startInclusive.atStartOfDay(zone).toInstant().toString()

    fun endTimestamp(zone: ZoneId): String = endExclusive.atStartOfDay(zone).toInstant().toString()

    companion object {
        val businessZone: ZoneId = ZoneId.of("America/Mexico_City")

        fun current(): RewardPeriod = forDate(LocalDate.now(businessZone))

        fun forDate(date: LocalDate): RewardPeriod {
            val start = if (date.monthValue <= 6) LocalDate.of(date.year, 1, 1)
            else LocalDate.of(date.year, 7, 1)
            return RewardPeriod(start, start.plusMonths(6))
        }
    }
}

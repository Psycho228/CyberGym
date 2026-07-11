package com.nextrank.core.common.time

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Абстракция над системными часами.
 * Позволяет подменять реальные часы в тестах.
 */
interface Clock {
    fun now(): Instant
    fun nowUtc(): ZonedDateTime
    fun nowIn(zone: ZoneId): ZonedDateTime
}

/**
 * Реализация Clock, использующая системное время.
 */
class SystemClock : Clock {
    override fun now(): Instant = Instant.now()
    override fun nowUtc(): ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
    override fun nowIn(zone: ZoneId): ZonedDateTime = ZonedDateTime.now(zone)
}

/**
 * Тестовая реализация Clock с фиксированным временем.
 */
class TestClock(
    private val fixedInstant: Instant,
) : Clock {
    override fun now(): Instant = fixedInstant
    override fun nowUtc(): ZonedDateTime = fixedInstant.atZone(ZoneId.of("UTC"))
    override fun nowIn(zone: ZoneId): ZonedDateTime = fixedInstant.atZone(zone)
}

/**
 * Форматтер для отображения времени пользователю.
 */
object TimeFormatters {
    private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val DateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val DateTimeFormatterFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun formatTime(zdt: ZonedDateTime): String = zdt.format(TimeFormatter)
    fun formatDate(zdt: ZonedDateTime): String = zdt.format(DateFormatter)
    fun formatDateTime(zdt: ZonedDateTime): String = zdt.format(DateTimeFormatterFormatter)
}

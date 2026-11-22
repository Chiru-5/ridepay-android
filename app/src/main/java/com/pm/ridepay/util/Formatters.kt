package com.pm.ridepay.util

import com.pm.ridepay.data.model.RideStatus
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale


private val TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())


fun formatMoney(amount: BigDecimal): String = "₹"+ amount.stripTrailingZeros().toPlainString()

fun parseServerTime(value: String): LocalDateTime? =
    try{
    LocalDateTime.parse(value)
    }
    catch (e: DateTimeParseException){
        null
    }


fun formatTime(value: String): String = parseServerTime(value)?.format(TIME_FORMAT) ?: ""

fun isToday(value: String): Boolean = parseServerTime(value)?.toLocalDate() == LocalDate.now()




private val TIME_SECONDS_FORMAT = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.getDefault())
fun formatTimeWithSeconds(value: String): String=parseServerTime(value)?.format(TIME_SECONDS_FORMAT)?:""

private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("d/MM, h:mm a", Locale.getDefault())

fun formatDateTime(value: String?): String =
    value?.let { parseServerTime(it)?.format(DATE_TIME_FORMAT) }?: ""

fun formatDuration(start: String?, end: String?): String?{
    val from = start?.let { parseServerTime(it) }?: return null
    val to = end?.let { parseServerTime(it) } ?: return null
    val minutes = Duration.between(from, to).toMinutes()
    return if(minutes < 1) "under a minute" else "$minutes min"
}

fun RideStatus.label(): String = when(this){
    RideStatus.ACTIVE -> "In progress"
    RideStatus.COMPLETED -> "Completed"
    RideStatus.CANCELLED -> "Cancelled"
}
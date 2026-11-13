package com.pm.ridepay.util

import java.math.BigDecimal
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

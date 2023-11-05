package de.hda.fbi.nzse.sose22.iamsafe


import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

fun dateToRelativeTime (date: Date?): String {
    if(date == null) {
        return "unknown"
    }

    val now = Date()

    val comparisonSeconds = (now.time - date.time) / 1000

    var prefix = ""
    var suffix = ""

    if(comparisonSeconds < 0) {
        prefix = "in "
    } else {
        suffix = " ago"
    }

    var timeValue = floor((comparisonSeconds / 60 / 60).toDouble())
    var timeUnit = " hours"
    if(timeValue.toInt() == 1) {
        timeUnit = " hour"
    }

    if(timeValue > 48) {
        timeValue = floor(timeValue / 24)
        timeUnit = " days"
    }

    if(comparisonSeconds < 60 * 60) {
        timeUnit = " min"
        timeValue = floor((comparisonSeconds / 60).toDouble())
    }


    return prefix + timeValue.toInt().toString() + timeUnit + suffix
}

fun getMinutesAgo (from: Date?, to: Date?): Int {
    if(from == null || to == null) {
        return 0
    }
    return round((abs(to.time - from.time) / 1000 / 60).toDouble()).toInt()
}

/**
 * If date is more than 48 hours ago
 */
fun isLastStatusSetLongAgo (date: Date?): Boolean {
    if(date != null) {
        return getMinutesAgo(date, Date()) > 48 * 60
    }

    return true
}

fun isDatePast (date: Date): Boolean {
    return date.before(Date())
}

fun convertLongToTimestamp(time : Long) : Timestamp {
    val date = Date(time)
    return Timestamp(date)
}

fun timeInSecondsToDateString(timeSeconds : Long): String? {
    val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
    return simpleDateFormat.format(timeSeconds * 1000L)
}

const val makeMilSec = 1000

/*fun getTimezoneAdjustedDate (date: Date): Date {
    return Date.from(Instant.ofEpochMilli( date.time-date.timezoneOffset*60*1000))
}*/


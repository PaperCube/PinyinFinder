package studio.papercube.pinyinfinder.time

import java.text.SimpleDateFormat
import java.util.*

object FormattedTime {
    private val standardDateFormat get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US)

    fun getSimplyFormattedTime(date: Date? = null): String = standardDateFormat.format(date ?: Date())
}
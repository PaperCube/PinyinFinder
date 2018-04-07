package studio.papercube.pinyinfinder.datatransfer

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import studio.papercube.pinyinfinder.content.BeanObject

class AppStatisticsAdditionalInfoCollector(private val sharedPreferences: SharedPreferences) {
    var additionalInfo: AppStatisticsAdditionalInfo? = null

    private fun loadIfNecessary() {
        if (additionalInfo == null) {
            additionalInfo = AppStatisticsAdditionalInfo()
            loadUsageStatistics()
        }
    }

    private fun loadUsageStatistics() {
        with(additionalInfo!!.usageStatistics) {
            isUpdateDetectionEnabled = sharedPreferences.getBooleanOrNull("autoCheckForUpdate")
        }
    }

    fun collect(): AppStatisticsAdditionalInfo {
        loadIfNecessary()
        return additionalInfo!!
    }

    fun collectAsString(): String {
        return collect().toString()
    }

    override fun toString(): String {
        return collectAsString()
    }

    private fun SharedPreferences.getBooleanOrNull(key: String): Boolean? {
        return if (contains(key)) this.getBoolean(key, false) else null
    }
}

class AppStatisticsAdditionalInfo : BeanObject<AppStatisticsAdditionalInfo>() {
    var usageStatistics: UsageStatistics = UsageStatistics()

    override fun toString(): String {
        return Gson().toJson(this, object : TypeToken<AppStatisticsAdditionalInfo>() {}.type)
    }

    inner class UsageStatistics : BeanObject<UsageStatistics>() {
        var isUpdateDetectionEnabled: Boolean? = null
        var shareCount: Int = -1
        var isSharingNotificationDisabled: Boolean? = null
    }
}
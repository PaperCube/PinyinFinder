package studio.papercube.pinyinfinder.datatransfer

import android.content.SharedPreferences
import android.os.Build
import studio.papercube.pinyinfinder.content.BeanObject
import studio.papercube.pinyinfinder.content.Hex
import studio.papercube.pinyinfinder.datatransfer.InstallationIdMgr.getInstallationId
import studio.papercube.pinyinfinder.time.FormattedTime
import studio.papercube.pinyinfinder.update.Updater
import java.util.*

private object StaticDeviceInfo {
    fun getDetailedAndroidVersion(): String {
        val sdkInt = Build.VERSION.SDK_INT
        val inc = Build.VERSION.INCREMENTAL
        val fingerprint = Build.FINGERPRINT
        return mapOf(
                "SDK_INT" to sdkInt,
                "INCREMENTAL" to inc,
                "BUILD_FINGERPRINT" to fingerprint
        ).toString()
    }
}

object InstallationIdMgr {
    @JvmStatic
    fun getInstallationId(pref: SharedPreferences): String {
        val id = pref.getString("installationId", "Unknown")
        return if (id == "Unknown") {
            allocateNewId(pref)
        } else id
    }

    @JvmStatic
    fun setInstallationId(pref: SharedPreferences, id: String) {
        pref.edit()
                .putString("installationId", id)
                .apply()
    }

    @JvmStatic
    fun allocateNewId(pref: SharedPreferences): String {
        val id = Hex.toGroupedHexString(Random().nextLong())
        setInstallationId(pref, id)
        return id
    }
}

@Suppress("unused")
class AppLaunch private constructor() : BeanObject<AppLaunch>() {
    companion object {
        @JvmStatic
        fun create(pref: SharedPreferences): AppLaunch = AppLaunch().apply {
            installationId = getInstallationId(pref)
            deviceModel = Build.MODEL
            androidVersion = StaticDeviceInfo.getDetailedAndroidVersion()
            appVersion = Updater.currentVersionName
            additionalInfo = AppStatisticsAdditionalInfoCollector(pref).collectAsString()
            time = try {
                FormattedTime.getSimplyFormattedTime()
            } catch (e: IllegalArgumentException) {
                e.toString()
            }

            val launchCountReadFromPref = pref.getString("AppLaunchCount", "1").toLongOrNull() ?: 1
            launchCount = launchCountReadFromPref
            pref.edit()
                    .putString("AppLaunchCount", (launchCountReadFromPref + 1).toString())
                    .apply()
        }
    }

    var installationId: String? = null
    var deviceModel: String? = null
    var androidVersion: String? = null
    var appVersion: String? = null
    var additionalInfo: String? = null
    var timeStamp: Long? = System.currentTimeMillis()
    var time: String? = null
    var launchCount: Long? = 0
}

@Suppress("unused")
class Feedback(val text: String,
               configPref: SharedPreferences
) : BeanObject<Feedback>() {
    val androidVersion = StaticDeviceInfo.getDetailedAndroidVersion()
    val time: String = FormattedTime.getSimplyFormattedTime()
    val installationId: String = InstallationIdMgr.getInstallationId(configPref)
}


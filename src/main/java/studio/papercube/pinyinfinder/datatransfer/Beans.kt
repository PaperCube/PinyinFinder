package studio.papercube.pinyinfinder.datatransfer

import android.content.SharedPreferences
import android.os.Build
import studio.papercube.pinyinfinder.content.BeanObject
import studio.papercube.pinyinfinder.update.Updater
import java.util.*


@Suppress("unused")
class AppLaunch private constructor() : BeanObject<AppLaunch>() {
    companion object {
        @JvmStatic fun getInstallationId(pref: SharedPreferences): String {
            val id = pref.getString("installationId", "Unknown")
            return if (id == "Unknown") {
                allocateNewId(pref)
            } else id
        }

        @JvmStatic fun setInstallationId(pref: SharedPreferences, id: String) {
            pref.edit()
                    .putString("installationId", id)
                    .apply()
        }

        @JvmStatic fun allocateNewId(pref: SharedPreferences): String {
            val id = Random().nextLong().toString()
            setInstallationId(pref, id)
            return id
        }

        @JvmStatic fun create(pref: SharedPreferences): AppLaunch = AppLaunch().apply {
            installationId = getInstallationId(pref)
            deviceModel = Build.MODEL
            androidVersion = Build.VERSION.SDK_INT.toString()
            appVersion = Updater.currentVersionName
            additionalInfo = ""

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
    var launchCount: Long? = 0
}


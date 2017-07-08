package studio.papercube.pinyinfinder.datatransfer

import android.content.SharedPreferences
import android.os.Build
import cn.bmob.v3.BmobObject
import studio.papercube.pinyinfinder.update.Updator
import java.util.*

class AppLaunch : BmobObject() {
    companion object {
        @JvmStatic fun getInstallationId(pref: SharedPreferences): String {
            val id = pref.getString("installationId", "Unknown")
            return if(id == "Unknown"){
                allocateNewId(pref)
            } else id
        }

        @JvmStatic fun setInstallationId(pref: SharedPreferences, id: String) {
            pref.edit()
                    .putString("installationId", id)
                    .apply()
        }

        @JvmStatic fun allocateNewId(pref:SharedPreferences):String{
            val id = Random().nextLong().toString()
            setInstallationId(pref, id)
            return id
        }

        @JvmStatic fun create(pref: SharedPreferences): AppLaunch = AppLaunch().apply {
            installationId = getInstallationId(pref)
            deviceModel = Build.MODEL
            androidVersion = Build.VERSION.SDK_INT.toString()
            appVersion = Updator.currentVersionName
            additionalInfo = ""
        }
    }

    var installationId:String? = null
    var deviceModel: String? = null
    var androidVersion: String? = null
    var appVersion: String? = null
    var additionalInfo: String? = null
    var timeStamp: Long? = System.currentTimeMillis()
}
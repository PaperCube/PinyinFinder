package studio.papercube.pinyinfinder

import android.content.Context

class LegacyRemovingAssistant(private val context: Context) {
    companion object {
        @JvmStatic val legacyPackageNames = arrayOf("studio.papercube.pinyinfinder")
    }

    fun legacyPackageName(): String? {
        context.packageManager.run {
            return getInstalledPackages(0).firstOrNull { it.packageName.toLowerCase() in legacyPackageNames }?.packageName
        }
    }
}
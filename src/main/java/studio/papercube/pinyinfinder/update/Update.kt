package studio.papercube.pinyinfinder.update

import java.util.*

class Update {
    val attributes: MutableMap<String, String> = HashMap()
    val versions: MutableList<Version> = ArrayList()

    val title: String
        get() {
            return "更新到${getLatestVersion()?.versionName}"
        }

    val message: String
        get() {
            val latestVersionCode = getLatestVersionCode()
            return versions.filter { it.versionCode > Updater.currentVersionCode && it.versionCode <= latestVersionCode }
                    .sortedByDescending { it.versionCode }
                    .joinToString(separator = "\n", transform = Version::mergeMessages)
        }

    /**
     * 获得最新的版本号。如果最新版本未指定或者在转换字符串到数字的过程中出错了，那么返回-1
     *
     * @throws Exception 如果出现了奇怪的问题，那么将抛出这个异常
     */
    fun getLatestVersionCode(): Int {
        return try {
            attributes["latest"]?.let(String::toInt) ?: -1
        } catch (e: NumberFormatException) {
            -1
        }
    }

    fun getLatestVersion(): Version? {
        return versions.find { it.versionCode == getLatestVersionCode() }
    }

    /**
     * 这个方法永远不会抛出异常
     */
    fun hasLaterVersion(): Boolean {
        return try {
            getLatestVersionCode() > Updater.currentVersionCode
        } catch (e: Throwable) {
            false
        }
    }
}
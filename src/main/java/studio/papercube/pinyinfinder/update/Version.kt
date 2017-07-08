package studio.papercube.pinyinfinder.update

import java.util.*

class Version {
    var versionCode: Int = 0
    lateinit var versionName: String
    val messages: MutableList<String> = ArrayList()

    internal val attributes: MutableMap<String, String> = HashMap()

    fun addMessage(value: String) {
        messages.add(value)
    }

    fun getAttribute(key: String) = attributes["attribute/$key"]

    fun mergeMessages(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.appendln(versionName)
        for(msg in messages) stringBuilder.appendln(msg)

        return stringBuilder.toString()
    }
}
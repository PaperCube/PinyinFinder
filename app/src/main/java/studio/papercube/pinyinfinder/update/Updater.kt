package studio.papercube.pinyinfinder.update

import studio.papercube.pinyinfinder.update.GitRemoteAccess.Host.GITHUB
import studio.papercube.pinyinfinder.update.UpdateFailure.CausedBy.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

object Updater {
    var currentVersionCode: Int = 0
    var currentVersionName: String = "UNINITIALIZED"

    var updateHost: GitRemoteAccess.Host = GITHUB

    /**
     * 在当前线程执行检查更新。请注意，在ANDROID中，如果当前线程是UI线程，那么会崩溃。
     */
    fun checkForUpdate(): Update {
        try {
            val update = resolveUpdateFromStream(GitRemoteAccess(path = "pinyinfinder/updates.txt", host = updateHost).getInputStream())
            return update
        } catch (e: Throwable) {
            when (e) {
                is UpdateFailure -> throw e
                is IOException -> throw UpdateFailure(CONNECTION_FAILURE, causeException = e)
                else -> throw UpdateFailure(UNKNOWN, causeException = e)
            }
        }
    }

    fun whenUpdateFound(action: (Update) -> Unit) {
        Thread {
            try {
                val update = checkForUpdate()
                if (update.hasLaterVersion()) action(update)
            } catch (ignored: Exception) {
            }
        }.apply {
            isDaemon = true
            name = "Daemon Update Checker"
            start()
        }
    }

    private fun resolveUpdateFromStream(inputStream: InputStream): Update {
        val update = Update()

        BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")), 32).use {
            while (true) {
                val line = it.nextTrimmedLine() ?: return@use
                if (line == "[version]") break
                else {
                    update.attributes.put(line.split(delimiters = "=", limit = 2))
                }
            }

            while (true) {
                val version = Version()
                val versionDescription = it.nextTrimmedLine()?.split(delimiters = ",", limit = 2) ?: return@use
                if (versionDescription.size > 1) {
                    version.versionCode = versionDescription[0].toInt()
                    if (version.versionCode <= currentVersionCode) return@use
                    version.versionName = versionDescription[1]
                } else throw UpdateFailure(CONTENT_BAD_FORMATTED, additionalMessage = "非法的版本号声明")

                while (true) {
                    val line = it.nextTrimmedLine() ?: break
                    if (line.startsWith("attribute/")) version.attributes.put(line.split(delimiters = "=", limit = 2))
                    else if (line.startsWith("-")) version.messages.add(line.substring(1))
                    else if (line == "[version]") break
                }

                update.versions.add(version)

            }
        }

        return update
    }

    private fun BufferedReader.nextTrimmedLine(): String? {
        while (true) {
            val untrimmedLine = readLine() ?: return null

            val trimmed = untrimmedLine.trim()
            if (trimmed.isEmpty()) continue
            else if (trimmed.startsWith("#")) continue
            else return trimmed
        }
    }

    private fun <T> MutableMap<in T, in T>.put(values: List<T>): Boolean {
        try {
            put(values[0], values[1])
            return true
        } catch(e: IndexOutOfBoundsException) {
            return false
        }
    }


}
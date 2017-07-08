package studio.papercube.pinyinfinder.update

import studio.papercube.pinyinfinder.update.GitRemoteAccess.Host.GITHUB
import studio.papercube.pinyinfinder.update.GitRemoteAccess.Host.OSCHINA
import java.io.InputStream
import java.net.URL

class GitRemoteAccess(
        val host: Host = GITHUB,
        val user: String = "PaperCube",
        val repository: String = "_RemoteAccess",
        val branch: String = "master",
        val path: String) {

    fun getInputStream(): InputStream {
        return when (host) {
            GITHUB -> URL("${host.hostUrl}/$user/$repository/raw/$branch/$path").openConnection().inputStream
            OSCHINA -> URL("${host.hostUrl}/$user/${repository.removeStartingUnderlines()}/raw/$branch/$path")
                    .openConnection()
                    .apply { this.connectTimeout=10000 }
                    .inputStream
        }
    }

    private fun String.removeStartingUnderlines(): String {
        val firstLetter = (0..this.length - 1).firstOrNull { this[it].isLetter() } ?: 0
        return if (firstLetter == 0) this else this.substring(firstLetter)
    }

    enum class Host(val hostUrl: String) {
        GITHUB("https://github.com"),
        OSCHINA("https://git.oschina.net");
    }
}
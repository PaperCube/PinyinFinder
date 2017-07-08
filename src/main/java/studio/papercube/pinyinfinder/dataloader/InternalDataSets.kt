package studio.papercube.pinyinfinder.dataloader

import java.io.InputStream
import java.util.*

/**
 *
 */
enum class InternalDataSets(private val url: String, override val dataName: String) : DataSet {
    GRADE2014("/data/grade2014.edt", "2014级"),
    GRADE2015("/data/grade2015.edt", "2015级(beta)"),
    GRADE2016("/data/grade2016.edt", "2016级");

    companion object {
        @JvmStatic val targetYear = 2016

        @JvmStatic fun hasExpired(): Boolean {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            val YEAR = calendar.get(Calendar.YEAR)
            val MONTH = calendar.get(Calendar.MONTH) + 1
            return !((YEAR == targetYear && MONTH > 9) || (YEAR == targetYear + 1 && MONTH < 9))
        }
    }

    override val inputStream: InputStream
        get() {
            return SimpleTranformInputStream(javaClass.getResourceAsStream(url))
        }

}
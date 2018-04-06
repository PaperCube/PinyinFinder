package studio.papercube.pinyinfinder

abstract class PinyinMatchResult {
    companion object {
        const val FLAG_MATCH_IN_NAME = 0x1L
        const val FLAG_MATCH_IN_CLASS = 0x2L
    }

    abstract val startInclusive: Int
    abstract val endExclusive: Int
    abstract val original: String
    open val flagSource get() = 0L
    open val flagError get() = 0L
    fun truncate(): String {
        return original.substring(startInclusive, endExclusive)
    }
}

abstract class FlagMutablePinyinMatchResult : PinyinMatchResult() {
    override var flagSource: Long = 0L
    override var flagError: Long = 0L
}

data class StandardPinyinMatchResult(
        override val startInclusive: Int,
        override val endExclusive: Int,
        override val original: String,
        override var flagSource: Long = 0L
) : FlagMutablePinyinMatchResult()

class ResultMatchAll(override val original: String,
                     override val flagSource: Long) : PinyinMatchResult() {
    override val startInclusive: Int
        get() = 0
    override val endExclusive: Int
        get() = original.length
}
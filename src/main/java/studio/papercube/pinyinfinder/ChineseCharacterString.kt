package studio.papercube.pinyinfinder

import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper

open class ChineseCharacterString(val value: String) {

    companion object {
        private val T9_MAP = "*/*/abc/def/ghi/jkl/mno/pqrs/tuv/wxyz".split("/")
        private const val INT_VALUE_OF_CHAR_ZERO = '0'.toInt()
    }

    fun matchPinyinAbbreviation(shortPinyin: String, requireLengthMatch: Boolean = false): FlagMutablePinyinMatchResult? {
        try {
            if (shortPinyin.length > value.length) return null

            val lowerCase = shortPinyin.toLowerCase()

            shortPinyinLoop@ for ((index, firstLetterToSearch) in lowerCase.withIndex()) { //iterate over letters
                if (firstLetterToSearch == '*' || //if is wildcard
                        firstLetterToSearch == value[index] || // if it is itself the same character in corresponding place
                        firstLetterToSearch isRepresentationInT9Of value[index]) { //if number matches
                    continue //search for next letter
                }

                if (firstLetterToSearch !in 'a'..'z') return null //if fails to meet all requirements, stop.

                @Suppress("LoopToCallChain") //the advice suggested by the warning can't be accepted.
                for (possiblePinyin in value[index].toPinyinArray(PinyinFormat.WITHOUT_TONE)) { // iterate over possible pinyin of that chinese character
                    if (firstLetterToSearch == possiblePinyin[0]) continue@shortPinyinLoop // matching first
                }

                // nothing matches
                return null
            }

            return if (!requireLengthMatch || lowerCase.length == value.length) {
                StandardPinyinMatchResult(0, shortPinyin.length, value)
            } else {
                null
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun matchFullPinyin(fullPinyin: String,
                        allowMiddleSearch: Boolean = false,
                        separator: String = FilterPolicy.fullPinyinSeparator,
                        requireLengthMatch: Boolean): FlagMutablePinyinMatchResult? {
        val fullPinyinParts = fullPinyin.toLowerCase().split(separator)
        val partSize = fullPinyinParts.size
        val acceptable = if (partSize > value.length)
            false
        else
            fullPinyinParts.withIndex().all { (index, part) ->
                // iterate over all pinyin parts
                value[index].toPinyinArray(PinyinFormat.WITHOUT_TONE).any {
                    when {
                        allowMiddleSearch -> part in it
                        part == "*" -> true
                        index < partSize - 1 -> part == it
                        else -> it.startsWith(part)
                    }
                }
            } && (!requireLengthMatch || partSize == value.length)
        return if (acceptable) StandardPinyinMatchResult(0, partSize, value) else null
    }

    fun pinyinAbbreviationMatches(shortPinyin: String, requireLengthMatch: Boolean = false): Boolean {
        return matchPinyinAbbreviation(shortPinyin, requireLengthMatch) != null
    }

    fun fullPinyinMatches(fullPinyin: String,
                          allowMiddleSearch: Boolean = false,
                          separator: String = FilterPolicy.fullPinyinSeparator,
                          requireLengthMatch: Boolean): Boolean {
        return matchFullPinyin(fullPinyin, allowMiddleSearch, separator, requireLengthMatch) != null
    }

    private infix fun Char.isRepresentationInT9Of(chineseCharacter: Char): Boolean {
        return this.toInt() - INT_VALUE_OF_CHAR_ZERO isRepresentationInT9Of chineseCharacter
    }

    private infix fun Int.isRepresentationInT9Of(chineseCharacter: Char): Boolean {
        return if (this in 2..9) {
            val expectedFirstLetters = T9_MAP[this]
            val foundFirstLetters = chineseCharacter.toPinyinArray().map { it.first().toLowerCase() }
            expectedFirstLetters.any { it in foundFirstLetters }
        } else false
    }


    @Suppress("NOTHING_TO_INLINE")
    private inline fun Char.toPinyinArray(pinyinFormat: PinyinFormat = PinyinFormat.WITH_TONE_MARK) = PinyinHelper.convertToPinyinArray(this, pinyinFormat)

    override fun toString() = value
}

class FilterPolicy {
    companion object {
        @JvmStatic
        val fullPinyinSeparator = " "
    }
}

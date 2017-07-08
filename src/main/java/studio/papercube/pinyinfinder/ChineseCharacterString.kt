package studio.papercube.pinyinfinder

import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper

private val t9Map = "*/*/abc/def/ghi/jkl/mno/pqrs/tuv/wxyz".split("/")

open class ChineseCharacterString(val value: String) {
    fun pinyinAbbreviationMatches(shortPinyin: String, requireLengthMatch: Boolean = false): Boolean {
        try {
            if (shortPinyin.length > value.length) return false

            val processed = shortPinyin.toLowerCase()

            shortPinyinLoop@ for ((index, firstLetterToSearch) in processed.withIndex()) {
                if (firstLetterToSearch == '*' ||
                        firstLetterToSearch == value[index] ||
                        firstLetterToSearch isRepresentationInT9Of value[index])
                    continue

                if(firstLetterToSearch !in 'a'..'z') return false

                for (possiblePinyin in value[index].toPinyinArray(PinyinFormat.WITHOUT_TONE)) {
                    if (firstLetterToSearch == possiblePinyin[0]) continue@shortPinyinLoop
                }

                return false
            }

            return !requireLengthMatch || processed.length == value.length
        } catch (e: Throwable) {
            return false
        }
    }

    private val ZERO_IN_CHAR = '0'.toInt()
    private infix fun Char.isRepresentationInT9Of(chineseCharacter: Char):Boolean{
        return this.toInt() - ZERO_IN_CHAR isRepresentationInT9Of chineseCharacter
    }

    private infix fun Int.isRepresentationInT9Of(chineseCharacter: Char): Boolean {
        if (this in 2..9) {
            val expectedFirstLetters = t9Map[this]
            val foundFirstLetters = chineseCharacter.toPinyinArray().map{it.first().toLowerCase()}
            return expectedFirstLetters.any{ it in foundFirstLetters}
        } else return false
    }


    @Suppress("NOTHING_TO_INLINE")
    private inline fun Char.toPinyinArray(pinyinFormat: PinyinFormat = PinyinFormat.WITH_TONE_MARK) = PinyinHelper.convertToPinyinArray(this, pinyinFormat)

    override fun toString() = value
}

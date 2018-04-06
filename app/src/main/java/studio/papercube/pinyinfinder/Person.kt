package studio.papercube.pinyinfinder

import studio.papercube.pinyinfinder.PinyinMatchResult.Companion.FLAG_MATCH_IN_CLASS
import studio.papercube.pinyinfinder.PinyinMatchResult.Companion.FLAG_MATCH_IN_NAME

class Person {
    val from: String
    val name: String
    var originalClass: String? = null

    constructor(rawText: String) {
        val elements = rawText.split(",")
        from = elements[0].trim()
        name = elements[1].trim()
        elements.getOrNull(2)?.let { originalClass = it }
    }

    constructor(from: String, name: String) {
        this.from = from
        this.name = name
    }

    fun matches(pattern: String, requireLengthMatch: Boolean = false): Boolean {
        val chineseCharacterString = ChineseCharacterString(name)
        return chineseCharacterString.pinyinAbbreviationMatches(pattern, requireLengthMatch) ||
                pattern in name ||
                pattern in from ||
                chineseCharacterString.fullPinyinMatches(pattern, requireLengthMatch = requireLengthMatch)
    }

    fun tryMatch(pattern: String, requireLengthMatch: Boolean = false): PersonMatch {
        val chineseCharacterString = ChineseCharacterString(name)
        val pinyinMatchResult = chineseCharacterString.matchPinyinAbbreviation(pattern, requireLengthMatch)?.sourceFlag(FLAG_MATCH_IN_NAME)
                ?: pattern.checkTextOccurrenceIn(name)?.sourceFlag(FLAG_MATCH_IN_NAME)
                ?: pattern.checkTextOccurrenceIn(from)?.sourceFlag(FLAG_MATCH_IN_CLASS)
                ?: chineseCharacterString.matchFullPinyin(pattern, requireLengthMatch = requireLengthMatch)?.sourceFlag(FLAG_MATCH_IN_NAME)

        return PersonMatch(this, pinyinMatchResult)
    }

    private fun String.checkTextOccurrenceIn(text: String): FlagMutablePinyinMatchResult? {
        val index = text.indexOf(this)
        return if (index == -1) null
        else StandardPinyinMatchResult(index, index + length, text)
    }

    private fun FlagMutablePinyinMatchResult.sourceFlag(flag: Long) = apply {
        flagSource = flag
    }

    override fun toString(): String {
        @Suppress("RemoveCurlyBracesFromTemplate")
        return if (originalClass.isNullOrBlank()) "$name, $from"
        else "$name, $from (原${originalClass}班)"
    }
}

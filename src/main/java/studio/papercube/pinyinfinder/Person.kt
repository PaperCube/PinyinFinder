package studio.papercube.pinyinfinder

class Person {
    val from: String
    val name: String

    constructor(rawText: String) {
        val elements = rawText.split(",")
        from = elements[0].trim()
        name = elements[1].trim()
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

    override fun toString(): String {
        return "$name, $from"
    }
}

package studio.papercube.pinyinfinder

class Person {
    val from: String
    val name: String
    var originalClass:String? = null

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

    override fun toString(): String {
        @Suppress("RemoveCurlyBracesFromTemplate")
        return if(originalClass == null) "$name, $from"
        else "$name, $from (原${originalClass}班)"
    }
}

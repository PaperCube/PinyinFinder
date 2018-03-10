package studio.papercube.pinyinfinder

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import studio.papercube.pinyinfinder.graphics.Colors

class PersonMatch(val person: Person,
                  val matchResult: PinyinMatchResult?,
                  val flagMatchResult: Long = 0) {
    val matchSucceeded get() = matchResult != null

    override fun toString(): String {
        return person.toString()
    }

    fun toSpannableString(): CharSequence {
        if (matchResult == null) return toString()
        val b = SpannableStringBuilder()
        val namePart = SpannableString(person.name)
        val classPart = SpannableString(person.from)
        with(matchResult) {
            when {
                flagSource hasFlag PinyinMatchResult.FLAG_MATCH_IN_NAME -> namePart
                flagSource hasFlag PinyinMatchResult.FLAG_MATCH_IN_CLASS -> classPart
                else -> null
            }?.setSpan(ForegroundColorSpan(Colors.colorAccent),
                    matchResult.startInclusive,
                    matchResult.endExclusive,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        b.append(namePart)
                .append(", ")
                .append(classPart)
        if(!person.originalClass.isNullOrBlank()){
            b.append(" (原${person.originalClass}班) ")
        }

        return b
    }
}

@file:Suppress("unused")

package studio.papercube.pinyinfinder.widgets

import android.text.SpannableStringBuilder
import android.text.Spanned

private typealias SpanBuilder = SpannableStringBuilder

fun SpanBuilder.appendSpan(text:CharSequence?, what:Any?, flags: Int = Spanned.SPAN_INCLUSIVE_EXCLUSIVE) = apply {
    val start = length
    val end = start + text.toString().length
    append(text.toString())
    setSpan(what, start, end, flags)
}

fun SpanBuilder.spanned(what: Any?, start: Int, end: Int, flags: Int) = apply { setSpan(what, start, end, flags) }

fun SpanBuilder.lineAppended(text: CharSequence?, what: Any?, flags: Int = Spanned.SPAN_INCLUSIVE_EXCLUSIVE) = apply {
    appendSpan(text, what, flags)
    append('\n')
}

inline fun SpanBuilder.lineAppendedIf(
        condition: Boolean,
        what: Any?,
        flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        textSupplier: () -> CharSequence) = apply {
    if (condition) lineAppended(textSupplier(), what, flags)
}

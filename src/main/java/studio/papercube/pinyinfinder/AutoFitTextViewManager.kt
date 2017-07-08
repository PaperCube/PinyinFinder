package studio.papercube.pinyinfinder

import android.widget.TextView

open class AutoFitTextViewManager @JvmOverloads constructor(val textView: TextView, var defaultVisibility: Int = TextView.GONE) {
    companion object {
        @JvmStatic protected var strElementId: Int = 0
    }

    private val strings: MutableList<StringElementRef> = ArrayList()

    private var text: String?
        get() {
            return textView.text.toString()
        }
        set(value) {
            value?.takeIf { !value.isEmpty() }?.let {
                textView.text = value
                textView.visibility = TextView.VISIBLE
            } ?: run {
                textView.text = ""
                textView.visibility = defaultVisibility
            }
        }

    fun setColor(color: Int) {
        textView.setTextColor(color)
    }

    fun append(str: String): StringElementRef {
        val ref = StringElementRef(str)
        strings.add(ref)
        update()
        return ref
    }

    fun clearAll() {
        strings.clear()
        update()
    }

    fun remove(strRef: StringElementRef) {
        strings.remove(strRef)
        update()
    }

    protected fun update() {
        text = strings.joinToString(separator = "\n") { it.value }
    }

    open class StringElementRef(str: String) : Comparable<StringElementRef> {
        override fun compareTo(other: StringElementRef): Int {
            return other.id - this.id
        }

        override fun equals(other: Any?): Boolean {
            return (other as? StringElementRef)?.let { it.id == this@StringElementRef.id } ?: false
        }

        override fun hashCode(): Int {
            return 10824 * id + 619
        }

        override fun toString(): String {
            return value
        }

        val id = strElementId++
        val value = str
    }
}
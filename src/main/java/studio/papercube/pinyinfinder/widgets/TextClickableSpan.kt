package studio.papercube.pinyinfinder.widgets

import android.content.Context
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import studio.papercube.pinyinfinder.graphics.Colors

open class TextClickableSpan(context: Context) : ClickableSpan() {
    private var onClick: ((View?) -> Unit)? = null
    fun setOnClick(onClick: (View?) -> Unit) = apply {
        this.onClick = onClick
    }

    override fun onClick(widget: View?) {
        onClick?.invoke(widget)
    }

    override fun updateDrawState(ds: TextPaint?) {
        super.updateDrawState(ds)
        if (ds != null) {
            ds.color = Colors.colorLinkBlue
            ds.isUnderlineText = false
        }
    }
}
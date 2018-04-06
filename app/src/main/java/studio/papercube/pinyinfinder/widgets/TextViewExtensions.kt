package studio.papercube.pinyinfinder.widgets

import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView

fun TextView.autoHidden() = apply {
    if(text.isEmpty()) visibility = GONE
    else visibility = VISIBLE
}
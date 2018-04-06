package studio.papercube.pinyinfinder

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Looper
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View

@Suppress("NOTHING_TO_INLINE")
inline fun View.createSnackBar(text:String, longSnackbar:Boolean) = createSnackBar(text,if(longSnackbar) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT)

fun View.createSnackBar(text: String, length: Int = Snackbar.LENGTH_SHORT): Snackbar {
    return Snackbar
            .make(this, text, length)
            .apply { show() }
}

fun View.createSnackBar(text: String, length: Int = Snackbar.LENGTH_SHORT, actionName: String, action: (View?, Snackbar) -> Unit): Snackbar {
    return Snackbar
            .make(this, text, length)
            .apply {
                setAction(actionName) { view -> action(view, this) }
                show()
            }
}

fun Activity.createProgressDialog(msg: String, cancellable: Boolean = false, task: () -> Unit) {
    val progressDialog = ProgressDialog.show(this, null, msg, true, cancellable)
    Thread {
        task()
        progressDialog.dismiss()
    }.start()
}

fun Context.createMessageDialog(msg: String) {
    AlertDialog.Builder(this)
            .setMessage(msg)
            .setPositiveButton(R.string.text_confirm_ok) { _, _ -> }
            .create()
            .show()

}

fun Context.createMessageDialog(msgId: Int) {
    AlertDialog.Builder(this)
            .setMessage(msgId)
            .setPositiveButton(R.string.text_confirm_ok) { _, _ -> }
            .create()
            .show()
}

inline fun looperPrepared(task: () -> Unit) {
    Looper.prepare()
    task()
    Looper.loop()
}

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)


open class AfterTextChangedListener(val action: (String) -> Unit) : TextWatcher {
    override fun afterTextChanged(p0: Editable?) {
        action(p0?.toString() ?: "")
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }
}


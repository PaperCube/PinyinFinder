package studio.papercube.pinyinfinder

import android.content.Context
import android.support.v7.app.AlertDialog
import studio.papercube.pinyinfinder.annotations.RunnableOnAnyThread
import studio.papercube.pinyinfinder.annotations.UiThreadRequired

/**
 * 经过单一对话框管理器显示的对话框，只能有一个在显示。这个不是线程安全的
 */
class SingleDialogManager @RunnableOnAnyThread constructor(){
    /**
     * 当前正在显示的对话框
     */
    private var currentDialog:AlertDialog? = null

    /**
     * 显示一个对话框。它会先把上一个对话框（如果有）关掉，然后再显示现在的。必须在UI线程上运行
     * @param dialog 要显示的对话框
     */
    @UiThreadRequired
    fun display(dialog: AlertDialog) {
        currentDialog?.dismiss()

        dialog.show()
        currentDialog=dialog
    }
}

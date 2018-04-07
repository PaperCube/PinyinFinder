package studio.papercube.pinyinfinder

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import studio.papercube.pinyinfinder.content.Hex.toHexString
import studio.papercube.pinyinfinder.content.LOG_TAG_PYF_GENERAL
import studio.papercube.pinyinfinder.content.LOG_TAG_PYF_UI_EVENTS
import studio.papercube.pinyinfinder.datatransfer.Feedback
import studio.papercube.pinyinfinder.datatransfer.createBmobPostRequest
import studio.papercube.pinyinfinder.datatransfer.newCallWith
import studio.papercube.pinyinfinder.datatransfer.sharedOkHttpClient

class FeedbackActivity : AppCompatActivity() {
    companion object {
        const val RESULT_CODE_SUCCESS = 2
    }

    private lateinit var topView: View
    private lateinit var feedbackPreferences: SharedPreferences
    private lateinit var configPreferences: SharedPreferences
    private lateinit var editTextFeedback: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        topView = findViewById(R.id.layout_top_of_feedback_activity)
        feedbackPreferences = getSharedPreferences("Feedback", Context.MODE_PRIVATE)
        configPreferences = getSharedPreferences("AppConfig", Context.MODE_PRIVATE)
        editTextFeedback = findViewById(R.id.edit_text_feedback)
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.subtitle = "反馈"
            supportActionBar.setHomeButtonEnabled(true)
            supportActionBar.setDisplayHomeAsUpEnabled(true) //display the back button on the action bar. use supportActionBar
        }
        editTextFeedback.text = savedFeedback.toEditable()
    }

    private var savedFeedback: String
        set(value) {
            feedbackPreferences.edit()
                    .putString("feedback", value)
                    .apply()
        }
        get() {
            return feedbackPreferences.getString("feedback", "")
        }

    private val textFeedbackOnUi: String get() = editTextFeedback.text.toString()

    private fun autoSave() {
        val text = textFeedbackOnUi
        if (!text.isBlank()) {
            savedFeedback = text
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId
        when (itemId) {
            android.R.id.home -> finish()
            else -> {
                Log.i(LOG_TAG_PYF_UI_EVENTS, "${this.javaClass.name}: Unhandled options item ${item?.itemId?.toHexString()}")
            }
        }
        return true
    }

    fun onClickSubmitFeedback(view: View) {
        val feedbackText = textFeedbackOnUi
        if (feedbackText.isBlank()) {
            topView.createSnackBar("请输入内容")
            return
        }

        savedFeedback = feedbackText

        val progressDialog = ProgressDialog.show(this, null, "正在发送", true, false)
        val submitThread = Thread {
            //TODO make it cancellable
            var failureDescriptor: Any? = null
            var responseBodyString: String? = null
            var failed = false
            try {
                val response = createBmobPostRequest("Feedback", Feedback(feedbackText, configPreferences).toJsonString())
                        .newCallWith(sharedOkHttpClient)
                        .execute()
                val responseCode = response.code()
                responseBodyString = response.body().toString()
                progressDialog.dismiss()
                if (responseCode !in 200..299) {
                    failed = true
                    failureDescriptor = responseCode
                } else onComplete()
            } catch (e: Exception) {
                failed = true
                failureDescriptor = e
            }

            progressDialog.dismiss()
            if (failed) onSubmitFailure(failureDescriptor, responseBodyString)
            else onComplete()
        }.apply {
            name = "FeedbackSubmitter"
        }.start()
    }

    private fun onComplete() {
        runOnUiThread {
            editTextFeedback.text.clear()
            savedFeedback = ""
            setResult(RESULT_CODE_SUCCESS)
            finish()
        }
    }

    private fun onSubmitFailure(obj: Any?, responseBodyString: String?) {
        runOnUiThread {
            val msg = (obj as? Throwable)?.message ?: obj?.toString() ?: "未知原因"
            val responseMsg = responseBodyString ?: "No response"
            (obj as? Throwable)?.printStackTrace()
                    ?: Log.e(LOG_TAG_PYF_GENERAL, "Failed to submit feedback: $msg. \n Response:$responseMsg")
            createMessageDialog("未能发送. ($msg)")
        }
    }

    override fun onPause() {
        super.onPause()
        autoSave()
    }

    override fun onDestroy() {
        super.onDestroy()
        autoSave()
    }
}

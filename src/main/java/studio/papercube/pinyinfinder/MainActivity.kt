package studio.papercube.pinyinfinder

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import studio.papercube.pinyinfinder.annotations.LongOperationAgainstUIThread
import studio.papercube.pinyinfinder.annotations.RunOnCurrentThread
import studio.papercube.pinyinfinder.annotations.RunnableOnAnyThread
import studio.papercube.pinyinfinder.annotations.UiThreadRequired
import studio.papercube.pinyinfinder.concurrent.Processor
import studio.papercube.pinyinfinder.concurrent.sharedThreadPool
import studio.papercube.pinyinfinder.content.LOG_TAG_PYF_GENERAL
import studio.papercube.pinyinfinder.dataloader.DataSet
import studio.papercube.pinyinfinder.dataloader.InternalDataSets
import studio.papercube.pinyinfinder.dataloader.MultiSourceDataLoader
import studio.papercube.pinyinfinder.datatransfer.AppLaunch
import studio.papercube.pinyinfinder.datatransfer.createBmobPostRequest
import studio.papercube.pinyinfinder.datatransfer.newCallWith
import studio.papercube.pinyinfinder.datatransfer.sharedOkHttpClient
import studio.papercube.pinyinfinder.graphics.Colors
import studio.papercube.pinyinfinder.localcommand.Command
import studio.papercube.pinyinfinder.update.GitRemoteAccess
import studio.papercube.pinyinfinder.update.Update
import studio.papercube.pinyinfinder.update.UpdateFailure
import studio.papercube.pinyinfinder.update.UpdateFailure.CausedBy.*
import studio.papercube.pinyinfinder.update.Updater
import studio.papercube.pinyinfinder.widgets.*

/**
 * 应用程序的主界面。第一个活动
 */
class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    companion object {
        const val REQUEST_START_FEEDBACK_ACTIVITY = 1
    }

    /**
     * 应用主界面的搜索框。对应[R.id.editTextSearch]。在[onCreate]方法中载入
     */
    private lateinit var editTextSearch: EditText

    /**
     * 搜索的结果在这里显示
     */
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: NamesRecyclerViewAdapter

    /**
     * 提示语。位于搜索框下面，结果列表的上面。
     */
    private lateinit var textInfo: TextView

    /**
     * 本界面覆盖范围最大的一个布局容器。
     */
    private lateinit var topView: View

    /**
     * 存放当前要搜索的所有人
     */
    private lateinit var persons: PersonList

    /**
     * 当前已经选择的数据集合（名单）。随着[loadData]的调用而更改。
     */
    @Volatile
    private var selectedDataSets: List<DataSet>? = null

    /**
     * 当前结果. 在Processor的第一次处理阶段被赋值
     */
    @Volatile
    private var currentResult: List<PersonMatch>? = null

    /**
     * 公共设置
     */
    private lateinit var sharedPreferences: SharedPreferences

    /**
     * 单一对话框控制器。
     */
    private val updateDialogMgr: SingleDialogManager = SingleDialogManager()

    private lateinit var additionalText: TextView

    private var isInternalDataSetExpired = false

    /**
     * 异步处理器。这个处理器是用来加载符合条件的姓名的。
     */
    @SuppressLint("SetTextI18n")
    private val processor = Processor { inputText: String ->
        val resultListUnsorted: List<PersonMatch> = when {
            inputText.isEmpty() -> emptyList() //如果要处理的缩写是空字符串，那么直接返回空列表
            inputText.startsWith("原") -> persons.matchByPreviousClass(inputText)
            else -> null
        } ?: persons.matchByShortPinyin(inputText)


        val result = resultListUnsorted
                .sortedBy { it.person.name.length }
                .apply { currentResult = this }
        result
    }.then { filteredResult ->
        runOnUiThread {
            recyclerViewAdapter.commitData(filteredResult)
            val selectedDataSetsCount = selectedDataSets?.size //已经选择的数据集合（名单）的数量。null代表名单还没有初始化。正常情况下它不应该是null
            textInfo.text = when {
                selectedDataSetsCount != 0 -> "在${selectedDataSetsCount ?: "?"}个名单中找到${filteredResult.size}条数据"
                else -> SpannableStringBuilder()
                        .append("你没有选择任何名单。")
                        .appendSpan("更改设置", TextClickableSpan(this@MainActivity).setOnClick {
                            showDataSelector()
                        })
            }
        }
    }.startThread()

//    private val permissionLock: ReentrantLock = ReentrantLock()
//    private val bmobPermissionGrantingCondition = permissionLock.newCondition()

    /**
     * 创建应用程序界面时调用。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //LATE-INIT VAR INITIALIZATIONS
        recyclerView = findViewById(R.id.listResults) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerViewAdapter = NamesRecyclerViewAdapter(this)
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        editTextSearch = findViewById(R.id.editTextSearch) as EditText
        recyclerView = findViewById(R.id.listResults) as RecyclerView
        textInfo = findViewById(R.id.textInfo) as TextView
        textInfo.movementMethod = LinkMovementMethod.getInstance() //???
        topView = findViewById(R.id.topView)
//        additionalTextMgr = AutoFitTextViewManager(findViewById(R.id.additionalText) as TextView)
        additionalText = findViewById(R.id.additionalText) as TextView

        sharedPreferences = getSharedPreferences("AppConfig", Context.MODE_PRIVATE)

        Updater.currentVersionCode = packageManager.getPackageInfo(packageName, 0).versionCode //指定当前应用的版本号
        Updater.currentVersionName = packageManager.getPackageInfo(packageName, 0).versionName //指定当前应用的版本

        createProgressDialog("正在加载数据") {
            loadPreferredData()
            if (InternalDataSets.hasExpired()) isInternalDataSetExpired = true
            editTextSearch.addTextChangedListener(AfterTextChangedListener { onTextChanged(it.trim()) }) //为输入框指定文字改变时的监听器

            recyclerViewAdapter.onItemLongClickListener = { personMatch ->
                //为列表指定长按监听器。
                onLongPressItem(personMatch)
            }

            try {
                val lastAppVersion = sharedPreferences.getInt("appVersion", -1)
                if (lastAppVersion == -1) {//如果首次运行
                    onFirstRun()
                } else if (lastAppVersion != Updater.currentVersionCode) {
                    onFirstRunAfterUpdate()
                }

                sharedPreferences.edit()
                        .putInt("appVersion", Updater.currentVersionCode)
                        .apply()
            } catch (ignored: Throwable) { //忽略一切异常
            }

            saveAppLaunchToBmob()

        }

        try {
            if (sharedPreferences.getBoolean("autoCheckForUpdate", true)) { //如果允许自动检查更新
                Updater.updateHost = try {
                    GitRemoteAccess.Host.valueOf(sharedPreferences.getString("updateHost", "UNKNOWN")) //指定远程服务器。默认是GITHUB
                } catch (e: Throwable) {
                    GitRemoteAccess.Host.GITHUB //有任何错误（极其极其不正常的情况下出现GITHUB不存在）则指定GITHUB
                }

                Updater.whenUpdateFound { update -> runOnUiThread { update.buildDialog() } } //如果有更新，那么就提示
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //impl removed
    }

    /**
     * 第一次运行应用时触发。既成事实上，它只在[onCreate]中且非UI线程上调用。
     */
    private fun onFirstRun() {
        val legacyPackageName = LegacyRemovingAssistant(this).legacyPackageName()
        runOnUiThread {
            if (legacyPackageName == null) {
                showHelpDialog()//显示帮助对话框。
            } else {
                showRemoveLegacyVersionDialog(legacyPackageName)
            }
        }

    }

    private fun onFirstRunAfterUpdate() {
    }

    private fun saveAppLaunchToBmob() {
        Log.d(LOG_TAG_PYF_GENERAL, "Saving app launch to bmob")
        sharedThreadPool.submit {
            try {
                createBmobPostRequest("AppLaunch", AppLaunch.create(sharedPreferences).toJsonString())
                        .newCallWith(sharedOkHttpClient)
                        .execute()
                        .let { response ->
                            Log.i(LOG_TAG_PYF_GENERAL, "Submit app launch complete. Return code: ${response.code()} (OK if 201)")
                        }
            } catch (e: Throwable) {
                Log.e(LOG_TAG_PYF_GENERAL, "Failed to submit app launch:$e")
            }
        }
    }


    /**
     * 当在搜索结果列表长按时触发
     * @param position 搜索结果索引
     */
    private fun onLongPressItem(position: Int): Boolean {
        return currentResult?.let { return onLongPressItem(it[position]) } ?: false
    }

    private fun onLongPressItem(item: PersonMatch): Boolean {
        try {
            item.person.name.copyToClipboard()
            topView.createSnackBar("已复制到剪贴板")
        } catch (e: Exception) {
            topView.createSnackBar("无法复制这个内容:$e")
            return false
        }
        return true
    }


    /**
     * 当菜单项被点击时触发。
     * @param item 哪个菜单项被点击了
     */
    @UiThreadRequired
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
        //一般菜单项
            R.id.menu_item_about -> createMessageDialog(R.string.text_about)
            R.id.menu_item_help -> showHelpDialog()
            R.id.menu_check_for_update -> requestCheckForUpdate()
            R.id.menu_select_data_set -> showDataSelector()

        //复制选项
            R.id.menu_copy_all_names -> currentResult.joinToStringOrNullWhenEmpty(separator = "，") { it.person.name }.copyOrNotify()
            R.id.menu_copy_all_names_with_classes -> currentResult.joinToStringOrNullWhenEmpty(separator = "，") { "${it.person.from}班${it.person.name}" }.copyOrNotify()
            R.id.menu_item_send_feedback -> startActivityForResult(
                    Intent(this, FeedbackActivity::class.java),
                    REQUEST_START_FEEDBACK_ACTIVITY
            )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_START_FEEDBACK_ACTIVITY -> when (resultCode) {
                FeedbackActivity.RESULT_CODE_SUCCESS->{
                    topView.createSnackBar("已发送")
                }
            }
        }
    }


    /**
     * 主界面加载的过程中，加载到选项菜单时触发。这时指定要应用哪个菜单。
     * 一般来说，这个方法在[onCreate]方法之后执行。
     * 据说里面的内容在[onCreate]方法里执行也可以。
     */
    @UiThreadRequired
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**常量。指定命令的标识前缀*/
    val COMMAND_PREFIX = "##"

    /**常量。指定命令的标识后缀*/
    val COMMAND_SUFFIX = "*#"

    /**
     * 搜索框的文字被改变时触发。
     * @param text 新的文字。
     */
    @UiThreadRequired
    private fun onTextChanged(text: String) {
        val trimmed = text.trim()//去除空字符
        if (trimmed.startsWith(COMMAND_PREFIX)) { //如果前缀已经输入
            if (trimmed.endsWith(COMMAND_SUFFIX)) { //如果后缀已经输入，那么这个命令已经闭合
                trimmed.removePrefix(COMMAND_PREFIX).removeSuffix(COMMAND_SUFFIX).executeLocalCommand() //去除前后缀然后执行命令
            } else {
                textInfo.text = "命令模式。输入*#来闭合命令" //命令还没闭合
            }
        } else {
            searchFor(trimmed) //如果不是命令，那么就是要搜索的东西。

            //TODO WARNING AND IMPROVEMENTS: SWAP LAMBDA AND OTHER ARGS. DataSet situation may change in the future. Be aware of these data-set specific codes.
            SpannableStringBuilder()
                    .lineAppendedIf((text.toIntOrNull() ?: 0) in 100..120, ForegroundColorSpan(Colors.colorError)) {
                        "抱歉，还没有高一级部的名单"
                    }
                    .lineAppendedIf((text.toIntOrNull() ?: 0) in 300..320, ForegroundColorSpan(Colors.colorError)) {
                        "没有高三级部的班级信息"
                    }
                    .lineAppendedIf(text.split(FilterPolicy.fullPinyinSeparator).any { it.length > 6 }, ForegroundColorSpan(Colors.colorAccent)) {
                        "使用全拼检索时，相邻汉字的拼音之间请用空格分隔"
                    }
                    .lineAppendedIf(isInternalDataSetExpired, ForegroundColorSpan(Colors.colorError)) {
                        "数据已过期。这是${InternalDataSets.TARGET_YEAR}-${InternalDataSets.TARGET_YEAR + 1}学年的。"
                    }
                    .let {
                        additionalText.text = it
                        additionalText.autoHidden()
                    }
        }
    }

    /**
     * 执行一个命令。
     * @receiver 去除命令前后缀的原始命令文本
     */
    @SuppressLint("SetTextI18n")
    @RunnableOnAnyThread
    private fun String.executeLocalCommand() {
        val command = Command(this)
        val parameters = command.parameters
        var commandExecuted = true

        @Suppress("CanBeVal")
        var longSnackBar = false
        val resultMessage: String? = when (command.name) {
            "update-source" -> {
                try {
                    val updateHost = GitRemoteAccess.Host.valueOf(parameters[0])
                    Updater.updateHost = updateHost
                    sharedPreferences.edit()
                            .putString("updateHost", updateHost.name)
                            .apply()

                    "Update host switched to ${parameters[0]}"
                } catch (e: Throwable) {
                    "Failed."
                }
            }
            "gc" -> {
                MemoryDisposer.disposeFully().toString()
            }
            else -> {
                commandExecuted = false
                editTextSearch.text = "$COMMAND_PREFIX${command.raw}".toEditable()
                "未知命令"
            }
        }

        if (commandExecuted) editTextSearch.text = "".toEditable()
        resultMessage?.run { topView.createSnackBar(this, longSnackBar) }
    }


    /**
     * 开始搜索一个名字缩写。
     * @param text 要搜索的文字
     */
    @SuppressLint("SetTextI18n") //抑制String Resource警告
    private fun searchFor(text: String) {
        textInfo.text = "正在加载"
        processor.process(text)
    }

    /**
     * 加载一个数据集合列表。
     * @param dataSetToLoad 要加载的数据集合列表
     */
    @LongOperationAgainstUIThread
    @RunnableOnAnyThread
    private fun loadData(dataSetToLoad: List<DataSet>) {
        persons = MultiSourceDataLoader.load(dataSetToLoad)
        selectedDataSets = dataSetToLoad
        topView.createSnackBar("已加载 ${persons.size} 条数据", actionName = "更改") { _, _ ->
            //            snackBar.dismiss()
            showDataSelector()
        }

        safeForceRefreshResults()
    }

    /**
     * 先读取上次选择的集合列表，然后再用[loadData]加载它
     */
    @LongOperationAgainstUIThread
    @RunnableOnAnyThread
    private fun loadPreferredData() {
        loadData(getStoredSelectedDataSetItems().toList())
    }

    /**
     * 显示帮助对话框
     */
    @UiThreadRequired
    private fun showHelpDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.how_to_use_dialog_title)
                .setMessage(R.string.how_to_use_dialog_tutorial)
                .setPositiveButton(R.string.text_confirm_ok) { _, _ -> }
                .create()
                .show()
    }

    /**
     * 请求检查更新。这会弹出一个不停止的进度条，因而必须在UI线程上运行。该方法立即返回。
     */
    @UiThreadRequired
    private fun requestCheckForUpdate() {
        sharedPreferences.edit()
                .putBoolean("autoCheckForUpdate", true)
                .apply()

        createProgressDialog("正在检查更新", cancellable = true) {
            try {
                val update = Updater.checkForUpdate()
                if (update.hasLaterVersion()) runOnUiThread { update.buildDialog() }
                else runOnUiThread { topView.createSnackBar("没有更新的版本") }
            } catch (e: UpdateFailure) {
                e.printStackTrace()
                val msg: String = when (e.causedBy) {
                    CONTENT_BAD_FORMATTED -> "无法解析更新信息"
                    CONNECTION_FAILURE -> "无法连接到网络"
                    else -> "未知错误"
                }

                runOnUiThread { topView.createSnackBar(msg) }
            } catch (e: Throwable) {
                e.printStackTrace()
                runOnUiThread { topView.createSnackBar(e.toString()) }
            }
        }

    }

    /**
     * 根据更新构建提示对话框。
     * 对话框的内容包括更新的标题，更新的内容，以及更新按钮，取消按钮，不再提醒按钮。其中前两项由[Update]类自己处理。
     * 构建对话框不会考虑是否真的有更新版本，这意味着即使没有新版本，更新对话框仍然会显示。
     *
     * 如果点击了不再提醒，那么下次启动应用就不再会自动检查更新，而不仅仅是不显示对话框。
     * 一旦在选项菜单中手动检查了更新，那么“不再提醒”就会失效。
     */
    @UiThreadRequired
    private fun Update.buildDialog() {
        val dialog = AlertDialog.Builder(this@MainActivity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("更新") { _, _ ->
                    try {
                        val url = getLatestVersion()?.getAttribute("url") ?: throw UpdateFailure(NO_URL_SPECIFIED)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        createMessageDialog("无法更新到此版本")
                    }
                }
                .setNegativeButton("取消") { _, _ ->
                }
                .setNeutralButton("不再提醒") { _, _ ->
                    sharedPreferences.edit()
                            .putBoolean("autoCheckForUpdate", false)
                            .apply()
                }
                .create()

        updateDialogMgr.display(dialog)
    }

    /**
     * 获得储存的已经选择的数据名单。只有内部数据集合[InternalDataSets]
     * @return 内部数据集合的数组。
     */
    @RunnableOnAnyThread
    private fun getStoredSelectedDataSetItems(): Array<out InternalDataSets> {
        val selected = sharedPreferences.getStringSet("selectedDataSets", HashSet<String>())
        return if (selected.isEmpty()) InternalDataSets.values() else selected.map { InternalDataSets.valueOf(it) }.toTypedArray()
    }

    /**
     * 显示“选择要显示的数据”对话框。它会获取所有的可用内部名单，然后获得已经选择的内部名单，并且勾选已经选择的内部名单的复选框。
     * 点击完成后，便会加载数据，最后再更新数据。
     */
    @UiThreadRequired
    private fun showDataSelector() {
        val internalDataSets = InternalDataSets.values()

        //把内部数据集合的选中情况映射到一个boolean数组里。
        val checkedItems = BooleanArray(internalDataSets.size) { internalDataSets[it].name in getStoredSelectedDataSetItems().map { it.name } }

        AlertDialog.Builder(this)
                .setTitle("选择要显示的数据")
                .setMultiChoiceItems(internalDataSets.map { it.dataName }.toTypedArray(), checkedItems) { _, which, isChecked ->
                    checkedItems[which] = isChecked //当选中情况发生变化进行标记
                }

                .setPositiveButton("完成") { _, _ ->

                    createProgressDialog("正在加载数据") {
                        val selected = ArrayList<InternalDataSets>()
                        (0..checkedItems.size - 1)
                                .filter { checkedItems[it] }
                                .mapTo(selected) { internalDataSets[it] } //根据选择映射表过滤出选中的。

                        loadData(selected)

                        sharedPreferences.edit()
                                .putStringSet("selectedDataSets", selected.map { it.name }.toSet())
                                .apply()

                    }

                }
                .setNegativeButton("取消") { _, _ -> }
                .create()
                .show()
    }

    /**
     * 显示一个对话框，提示用户删除旧版本。
     * @param legacyPackageName 旧版本的包名
     */
    @UiThreadRequired
    private fun showRemoveLegacyVersionDialog(legacyPackageName: String) {
        AlertDialog.Builder(this)
                .setTitle("删除过时的版本")
                .setMessage("你的设备上安装了过时的版本。你想要现在删除它吗？如果现在不删除，你可能就分不清哪个是新版本了")
                .setPositiveButton("现在删除") { _, _ ->
                    try {
                        Intent(Intent.ACTION_DELETE).apply {
                            data = Uri.parse("package:$legacyPackageName")
                            startActivity(this)
                        }
                    } catch (e: Exception) {
                        topView.createSnackBar("无法打开删除界面。请手动删除。")
                    }
                }
                .setNegativeButton("稍后我自己手动删除") { _, _ -> }
                .setCancelable(false)
                .create()
                .show()
    }

    /**
     * 把一个字符串复制到首要剪贴板，就是[ClipboardManager.setPrimaryClip]。没有错误处理（ANDROID API没有说会抛出错误）
     */
    private fun String.copyToClipboard() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.primaryClip = ClipData.newPlainText("name", this)
    }

    /**
     * 通过[copyToClipboard]把一个可空字符串复制到剪贴板。如果字符串是空的，那么创建一个SNACKBAR提示错误，否则复制并提示成功。
     *
     * @param messageSuccess 成功的消息。
     * @param messageWhenNullOrEmpty 失败的消息。
     */
    private fun String?.copyOrNotify(messageWhenNullOrEmpty: String = "没有可以复制的文字", messageSuccess: String = "已复制到剪贴板") {
        val message =
                if (this == null) messageWhenNullOrEmpty
                else {
                    copyToClipboard()
                    messageSuccess
                }

        topView.createSnackBar(message)
    }

    /**
     * 把一个任意格式的列表经过一些处理组成一个单个字符串。
     * @receiver 可空的列表。
     * @param separator 每个元素之间的间隔符
     * @param mapping 对元素的转换
     * @return 如果列表是空的或者是null，那么返回null，否则返回结果。
     */
    private fun <T> List<T>?.joinToStringOrNullWhenEmpty(
            separator: String = ",",
            @RunOnCurrentThread mapping: (T) -> String): String? {
        return this?.run {
            if (isEmpty()) null
            else map(mapping).joinToString(separator)
        }
    }


    private fun safeForceRefreshResults() {
        runOnUiThread { onTextChanged(editTextSearch.text.toString()) } //更新结果。
    }

}



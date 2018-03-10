package studio.papercube.pinyinfinder.datatransfer

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

private fun String.toJsonRequestBody():RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), this)
fun String.bmobTableNameToURL() = "https://api.bmob.cn/1/classes/$this"

fun createBasicRequestBuilder():Request.Builder = Request.Builder()
        .addHeader("X-Bmob-Application-Id", BmobKeys.APPLICATION_ID)
        .addHeader("X-Bmob-REST-API-Key", BmobKeys.REST_API_KEY)

fun Request.newCallWith(okHttpClient: OkHttpClient) = okHttpClient.newCall(this)


fun createBmobPostRequest(tableName:String, json:String) = createBasicRequestBuilder()
        .url(tableName.bmobTableNameToURL())
        .post(json.toJsonRequestBody())
        .build()

fun createBmobPutRequest(tableName:String, objectId:String, json:String) = createBasicRequestBuilder()
        .url(tableName.bmobTableNameToURL() + "/$objectId")
        .put(json.toJsonRequestBody())
        .build()

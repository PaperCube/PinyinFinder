# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Program Files (x86)\Android\pinyinfinder-sdk/tools/proguard/proguard-pinyinfinder.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class **
-keepattributes EnclosingMethod, Signature,*Annotation*

-ignorewarnings

# ########################################
# okhttp
# ########################################

-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

# 确保JavaBean不被混淆-否则gson将无法将数据解析成具体对象
-keep class * extends studio.papercube.pinyinfinder.content.BeanObject {
    *;
}

-keep class * extends java.io.Serializable {
    *;
}

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
# Gson specific classes
-dontwarn sun.misc.**
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

##---------------End: proguard configuration for Gson  ----------

## ########################################
## Bmob
## ########################################
#
## keep BmobSDK
#-dontwarn cn.bmob.v3.**
#-keep class cn.bmob.v3.** {*;}
#
## 确保JavaBean不被混淆-否则gson将无法将数据解析成具体对象
#-keep class * extends cn.bmob.v3.BmobObject {
#    *;
#}
#
## keep BmobPush
#-dontwarn  cn.bmob.push.**
#-keep class cn.bmob.push.** {*;}
#
## keep okhttp3、okio
#-dontwarn okhttp3.**
#-keep class okhttp3.** { *;}
#-keep interface okhttp3.** { *; }
#-dontwarn okio.**
#
## keep rx
#-dontwarn sun.misc.**
#-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
# long producerIndex;
# long consumerIndex;
#}
#-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
# rx.internal.util.atomic.LinkedQueueNode producerNode;
#}
#-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
# rx.internal.util.atomic.LinkedQueueNode consumerNode;
#}
#
## 如果你需要兼容6.0系统，请不要混淆org.apache.http.legacy.jar
#-dontwarn android.net.compatibility.**
#-dontwarn android.net.http.**
#-dontwarn com.android.internal.http.multipart.**
#-dontwarn org.apache.commons.**
#-dontwarn org.apache.http.**
#-keep class android.net.compatibility.**{*;}
#-keep class android.net.http.**{*;}
#-keep class com.android.internal.http.multipart.**{*;}
#-keep class org.apache.commons.**{*;}
#-keep class org.apache.http.**{*;}
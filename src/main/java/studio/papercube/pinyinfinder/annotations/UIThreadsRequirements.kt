package studio.papercube.pinyinfinder.annotations

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.*

/**
 * 注明被修饰方法或构造方法必须在UI线程上运行
 */
@Target(CONSTRUCTOR, FUNCTION, PROPERTY_SETTER, PROPERTY_GETTER)
@Retention(SOURCE)
annotation class UiThreadRequired

/**
 * 注明被修饰的方法或构造方法可以在任何线程上运行
 */
@Target(CONSTRUCTOR, FUNCTION, PROPERTY_SETTER, PROPERTY_GETTER)
@Retention(SOURCE)
annotation class RunnableOnAnyThread

/**
 * 注明被修饰的方法或构造方法是一个费时操作且不应该在UI线程上运行
 */
@Target(CONSTRUCTOR, FUNCTION, PROPERTY_SETTER, PROPERTY_GETTER)
@Retention(SOURCE)
annotation class LongOperationAgainstUIThread

/**
 * 注明被修饰的参数将会在一个独立的UI线程上运行
 */
@Target(VALUE_PARAMETER)
@Retention(SOURCE)
annotation class RunOnNormalThread

/**
 * 注明被修饰的参数将会无论如何在UI线程上运行
 */
@Target(VALUE_PARAMETER)
@Retention(SOURCE)
annotation class RunOnUiThread


/**
 * 注明被修饰的参数将会在当前线程上运行
 */
@Target(VALUE_PARAMETER)
@Retention(SOURCE)
annotation class RunOnCurrentThread



package studio.papercube.pinyinfinder.annotations

import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*

@Target(FUNCTION)
@Retention(SOURCE)
annotation class ThreadSafe

@Target(FUNCTION)
@Retention(SOURCE)
annotation class ThreadUnsafe
package studio.papercube.pinyinfinder.concurrent

import studio.papercube.pinyinfinder.annotations.RunOnNormalThread
import studio.papercube.pinyinfinder.annotations.RunnableOnAnyThread
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Processor<T, out R>(@RunOnNormalThread private val task: (T) -> R) {
    private val lock: ReentrantLock = ReentrantLock()
    private val hasDataToProcess: Condition = lock.newCondition()
    private val processorThread: Thread

    @Volatile
    private var dataToProcess: T? = null

    @Volatile
    private var dataExpired = true

    private var secondaryComputing: ((T, R) -> Unit)? = null

    private val mainLoop: Runnable = Runnable {
        while (true) {
            var current: T? = null

            lock.withLock {
                if (dataExpired) {
                    hasDataToProcess.await()
                }

                current = dataToProcess
                dataExpired = true
            }

            val primaryComputingResult: R? = current?.let { task(it) }

            if (current === dataToProcess && primaryComputingResult != null) {
                secondaryComputing?.invoke(current!! ,primaryComputingResult)
            }
        }
    }


    init {
        processorThread = Thread(mainLoop).apply {
            isDaemon = true
            name = "Processor"
        }
    }

    @RunnableOnAnyThread fun process(data: T) {
        lock.withLock {
            dataToProcess = data
            dataExpired = false
            hasDataToProcess.signal()
        }
    }

    @RunnableOnAnyThread fun then(@RunOnNormalThread secondaryComputing: (T, R) -> Unit) = apply {
        this@Processor.secondaryComputing = secondaryComputing
    }


    @RunnableOnAnyThread fun startThread() = apply { processorThread.start() }
}

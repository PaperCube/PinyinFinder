package studio.papercube.pinyinfinder

import studio.papercube.pinyinfinder.dataloader.MultiSourceDataLoader

object MemoryDisposer {
    fun disposeFully(): MemoryState {
        MultiSourceDataLoader.dataSetCache.clear()
        System.gc()
        System.runFinalization()
        return MemoryState()
    }
}

class MemoryState {
    val free: Long
    val total: Long
    val max: Long

    init {
        val rt = Runtime.getRuntime()
        free = rt.freeMemory()
        total = rt.totalMemory()
        max = rt.maxMemory()
    }

    override fun toString() = "Free:${free.toAppropriateMemoryUnit()}, total:${total.toAppropriateMemoryUnit()}, max:${max.toAppropriateMemoryUnit()}"
}

val memoryUnits = arrayOf("B", "KB", "MB", "GB")
fun Long.toAppropriateMemoryUnit(): String {
    var temp = this.toDouble()
    var power = 0
    while (temp >= 1024) {
        power++
        temp /= 1024
    }

    return "${"%.2f".format(temp)} ${memoryUnits[power]}"
}

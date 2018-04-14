package studio.papercube.pinyinfinder.dataloader

import java.io.InputStream

open class SimpleTransformInputStream(private val inputStream: InputStream) : InputStream() {
    override fun read(): Int {
        return inputStream.read().let {
            if (it != -1) applyTransform(it)
            else -1
        }
    }

    override fun skip(n: Long): Long {
        return inputStream.skip(n)
    }

    override fun available(): Int {
        return inputStream.available()
    }

    override fun reset() {
        inputStream.reset()
    }

    override fun close() {
        inputStream.close()
    }

    override fun mark(readlimit: Int) {
        inputStream.mark(readlimit)
    }

    override fun markSupported(): Boolean {
        return inputStream.markSupported()
    }

    protected open fun applyTransform(value: Int): Int {
        return ((value shl 4) or (value ushr 4))
    }

}
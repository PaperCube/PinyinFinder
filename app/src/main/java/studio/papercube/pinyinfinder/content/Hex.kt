package studio.papercube.pinyinfinder.content

object Hex{
    @JvmStatic
    private val HEX_TABLE = "0123456789ABCDEF".toCharArray()

    @JvmStatic
    fun toGroupedHexString(longValue: Long):String {
        val strBuilder = StringBuilder(19)
        for(i in 7 downTo 0){
            val cByte = (longValue ushr (i * 8) and 0xFF).toInt()
            strBuilder.append(HEX_TABLE[cByte ushr 4 and 0xF])
            strBuilder.append(HEX_TABLE[cByte and 0xF])
            if(i > 0 && i % 2 == 0) strBuilder.append('-')
        }
        return strBuilder.toString()
    }
}
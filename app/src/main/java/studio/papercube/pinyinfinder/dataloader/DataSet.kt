package studio.papercube.pinyinfinder.dataloader

import java.io.InputStream

interface DataSet{
    val inputStream: InputStream
    val dataName:String? get() = null
}
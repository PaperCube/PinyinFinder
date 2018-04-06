package studio.papercube.pinyinfinder.dataloader

import studio.papercube.pinyinfinder.Person
import studio.papercube.pinyinfinder.PersonList
import java.io.BufferedReader

object MultiSourceDataLoader {
    /**
     * 缓存了一些数据集的转换结果。
     */
    internal val dataSetCache: MutableMap<DataSet, PersonList> = HashMap()

    /**
     * 使用可变长度参数加载多个数据集。它仅仅把可变长度参数转换成一个列表，剩下的操作和[load]完全相同。
     */
    fun load(vararg dataSets: DataSet) = load(dataSets.toList())

    /**
     * 加载数个数据集。
     *
     * @param dataSets The data set to load.
     * @return return the person list which contains all the persons included in the data set.
     */
    fun load(dataSets: List<DataSet>): PersonList {
        return PersonList().also { resultList ->
            for (dataSet in dataSets) {
                resultList.addAll(dataSetCache.getWhenPresent(dataSet) {
                    dataSet.toPersonList()
                })
            }

        }
    }


    private fun DataSet.toPersonList() = PersonList().apply { integrateInto(this) }


    /**
     * 将一个数据集聚合到一个[Person]的可变集合里。
     */
    private fun DataSet.integrateInto(collection: MutableCollection<Person>) {
        collection.apply {
            this@integrateInto.forEachLine {
                this@apply.add(Person(it))
            }
        }
    }


    /**
     * 遍历一个数据集输入流的每一行。
     */
    private inline fun DataSet.forEachLine(action: (String) -> Unit) {
        inputStream.bufferedReader(Charsets.UTF_8).use {
            it.forEachLineInlined(action)
        }
    }

    private inline fun forEachLine(dataSets: List<DataSet>, action: (String) -> Unit) {
        for (dataSet in dataSets) {
            dataSet.inputStream.bufferedReader(Charsets.UTF_8).use {
                it.forEachLineInlined(action)
            }
        }
    }

    private inline fun BufferedReader.forEachLineInlined(action: (String) -> Unit) {
        while (true) {
            val content = this.readLine() ?: break
            action(content)
        }
    }


    private inline fun <K, V> MutableMap<K, V>.getWhenPresent(key: K, computing: (K) -> V): V {
        return this[key] ?: run {
            computing(key).apply { this@getWhenPresent.put(key, this) }
        }
    }

}
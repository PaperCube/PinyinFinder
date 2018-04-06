package studio.papercube.pinyinfinder

import java.util.*

open class PersonList : ArrayList<Person> {
    companion object Factory{
        @JvmStatic fun delegate(list:List<Person>){

        }
    }
    constructor()
    constructor(c: MutableCollection<out Person>?) : super(c)

    fun toStringList(): List<String> {
        return (this as List<Person>).toStringList()
    }

}


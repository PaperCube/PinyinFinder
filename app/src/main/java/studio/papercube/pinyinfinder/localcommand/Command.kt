package studio.papercube.pinyinfinder.localcommand

class Command(val raw: String) {
    var parameters: List<String> private set
    var name: String private set

    init {
        val splited = raw.split(" ")
        name = splited[0]
        parameters = splited.drop(1)
    }


}
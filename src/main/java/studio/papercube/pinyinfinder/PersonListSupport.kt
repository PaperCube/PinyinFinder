package studio.papercube.pinyinfinder

fun List<Person>.toStringList(): List<String> {
    return map(Person::toString)
}

fun List<Person>.filterByShortPinyin(shortPinyin: String): List<Person> {
    val requireLengthMatch = shortPinyin.endsWith("!")
    val data = if (requireLengthMatch) shortPinyin.substring(0, shortPinyin.length - 1) else shortPinyin


    return filter {
        it.matches(data, requireLengthMatch)
    }
}

fun List<Person>.sortByNameLength(): List<Person> {
    return sortedBy { it.name.length }
}
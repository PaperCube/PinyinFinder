package studio.papercube.pinyinfinder

private const val SUFFIX_REQUIRE_LENGTH_MATCH = "!"

fun List<Person>.toStringList(): List<String> {
    return map(Person::toString)
}

fun List<Person>.filterByPreviousClass(inputText: String): List<Person>? {
    val givenOriginalClassLiteral = inputText.substring(1).toIntOrNull()
    //TODO COMPATIBILITY WARNING: data set may change. Be aware of these year- and data-set-specific codes.
    return if (givenOriginalClassLiteral == null || givenOriginalClassLiteral !in 100..199) null
    else filter { it.originalClass != null && it.originalClass?.toIntOrNull() == givenOriginalClassLiteral - 100 }
}

fun List<Person>.matchByPreviousClass(inputText: String): List<PersonMatch>? {
    val filtered = filterByPreviousClass(inputText) ?: return null
    return filtered.map { PersonMatch(it, ResultMatchAll(it.originalClass.toString(), 0)) }
}

fun List<Person>.filterByShortPinyin(shortPinyin: String): List<Person> {
    val requireLengthMatch = shortPinyin.endsWith(SUFFIX_REQUIRE_LENGTH_MATCH)
    val data = shortPinyin.removeSuffix(SUFFIX_REQUIRE_LENGTH_MATCH)

    return filter {
        it.matches(data, requireLengthMatch)
    }
}

fun List<Person>.matchByShortPinyin(shortPinyin: String): List<PersonMatch> {
    val requireLengthMatch = shortPinyin.endsWith(SUFFIX_REQUIRE_LENGTH_MATCH)
    val data = shortPinyin.removeSuffix(SUFFIX_REQUIRE_LENGTH_MATCH)

    return mapTo(ArrayList()) { p -> p.tryMatch(data, requireLengthMatch) }
            .filter { personMatch -> personMatch.matchSucceeded }
}

fun List<Person>.sortedByNameLength(): List<Person> {
    return sortedBy { it.name.length }
}

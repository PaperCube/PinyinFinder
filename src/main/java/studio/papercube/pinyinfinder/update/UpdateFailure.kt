package studio.papercube.pinyinfinder.update

class UpdateFailure(val causedBy: CausedBy,
                    val additionalMessage: String? = null,
                    val causeException: Throwable? = null) : RuntimeException() {

    enum class CausedBy {
        UNKNOWN,
        NO_URL_SPECIFIED,
        CONNECTION_FAILURE,
        PERMISSION_DENIED,
        CONTENT_BAD_FORMATTED;
    }

    override fun toString(): String {
        return "${super.toString()}\ncaused by $causedBy\n additionalMessage=$additionalMessage\ncauseException=$causeException"
    }
}
package studio.papercube.pinyinfinder

infix fun Long.hasFlag(flag: Long) = (this and flag) == flag
infix fun Long.removeFlag(flag: Long) = this and flag.inv()
infix fun Long.addFlag(flag: Long) = this or flag
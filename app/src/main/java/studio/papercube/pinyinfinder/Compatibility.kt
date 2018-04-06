package studio.papercube.pinyinfinder

import android.os.Build


class Compatibility {
    companion object {
        @JvmStatic val SDK_INT inline get() = Build.VERSION.SDK_INT
    }
}
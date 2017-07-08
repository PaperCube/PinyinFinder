package studio.papercube.pinyinfinder.content

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat

open class PermissionHelper {
    companion object {
        @JvmStatic fun <T> requestPermissions(context:T, requestCode: Int, vararg permissions: String)
                where T : Activity, T : ActivityCompat.OnRequestPermissionsResultCallback {
            val deniedPermissions = permissions.filter { ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
            if (deniedPermissions.isNotEmpty()) ActivityCompat.requestPermissions(context, deniedPermissions.toTypedArray(), requestCode)
        }
    }
}

fun Activity.permissionGranted(permission:String) = ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
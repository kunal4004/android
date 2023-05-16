package za.co.woolworths.financial.services.android.util

import androidx.appcompat.app.AppCompatActivity

abstract class RuntimePermissionActivity : AppCompatActivity(), PermissionResultCallback {

    private var permissionUtils: PermissionUtils? = null

    abstract fun onRuntimePermissionRequestGranted()
    abstract fun onRuntimePermissonRequestDenied()

    fun setUpRuntimePermission(permissions: ArrayList<String>) {
        permissionUtils = PermissionUtils(this, this)
        permissionUtils?.checkPermission(permissions, "Explain here why the app needs permissions", 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun permissionGranted(requestCode: Int) {
        onRuntimePermissionRequestGranted()
    }

    override fun permissionDenied(requestCode: Int) {
        onRuntimePermissonRequestDenied()
    }

    override fun neverAskAgain(requestCode: Int) {
        onRuntimePermissonRequestDenied()
    }

}
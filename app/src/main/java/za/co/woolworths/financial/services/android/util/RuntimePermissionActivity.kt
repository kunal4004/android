package za.co.woolworths.financial.services.android.util

import androidx.appcompat.app.AppCompatActivity
import java.util.ArrayList

abstract class RuntimePermissionActivity : AppCompatActivity(), PermissionResultCallback {

    private var permissionUtils: PermissionUtils? = null

    abstract fun onRuntimePermissionRequestGranted()
    abstract fun onRuntimePermissonRequestDenied()

    fun setUpRuntimePermission(permissions: ArrayList<String>?) {
        permissionUtils = PermissionUtils(this, this)
        permissionUtils?.check_permission(permissions, "Explain here why the app needs permissions", 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun PermissionGranted(request_code: Int) {
        onRuntimePermissionRequestGranted()
    }

    override fun PartialPermissionGranted(request_code: Int, granted_permissions: ArrayList<String>?) {
    }

    override fun PermissionDenied(request_code: Int) {
        onRuntimePermissonRequestDenied()
    }

    override fun NeverAskAgain(request_code: Int) {
        onRuntimePermissonRequestDenied()
    }

}
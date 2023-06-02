package za.co.woolworths.financial.services.android.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtils(var context: Context, permissionResultCallback: PermissionResultCallback) {
    private var currentActivity: Activity = context as AppCompatActivity
    var permissionResultCallback: PermissionResultCallback
    private var permissionList = ArrayList<String>()
    var listPermissionsNeeded = ArrayList<String>()
    private var requestCode = 0

    init {
        this.permissionResultCallback = permissionResultCallback
    }

    /**
     * Check the API Level & Permission
     *
     * @param permissions
     * @param requestCode
     */
    fun checkPermission(
        permissions: ArrayList<String>,
        requestCode: Int
    ) {
        permissionList = permissions
        this.requestCode = requestCode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkAndRequestPermissions(
                permissions,
                requestCode
            )
        ) {
            permissionResultCallback.permissionGranted(requestCode)
        } else {
            permissionResultCallback.permissionGranted(requestCode)
        }
    }

    /**
     * Check and request the Permissions
     *
     * @param permissions
     * @param requestCode
     * @return
     */
    fun checkAndRequestPermissions(permissions: ArrayList<String>, requestCode: Int): Boolean {
        if (permissions.size > 0) {
            listPermissionsNeeded = ArrayList()
            for (i in permissions.indices) {
                val hasPermission =
                    ContextCompat.checkSelfPermission(currentActivity, permissions[i])
                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permissions[i])
                }
            }
            if (listPermissionsNeeded.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    currentActivity,
                    listPermissionsNeeded.toTypedArray(),
                    requestCode
                )
                return false
            }
        }
        return true
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        when (requestCode) {
            1, 2, 3 -> if (grantResults.isNotEmpty()) {
                val perms: MutableMap<String, Int> = HashMap()
                run {
                    var i = 0
                    while (i < permissions.size) {
                        perms[permissions[i]] = grantResults[i]
                        i++
                    }
                }
                val pendingPermissions = ArrayList<String>()
                var i = 0
                while (i < listPermissionsNeeded.size) {
                    if (perms[listPermissionsNeeded[i]] != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                currentActivity,
                                listPermissionsNeeded[i]
                            )
                        ) pendingPermissions.add(
                            listPermissionsNeeded[i]
                        ) else {
                            permissionResultCallback.neverAskAgain(this.requestCode)
                            if (requestCode != 3) {
                                Toast.makeText(
                                    currentActivity,
                                    "Go to settings and enable permissions",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return
                        }
                    }
                    i++
                }
                if (pendingPermissions.size > 0) {
                    permissionResultCallback.permissionDenied(this.requestCode)
                } else {
                    permissionResultCallback.permissionGranted(this.requestCode)
                }
            }
        }
    }

    /**
     * Explain why the app needs permissions
     *
     * @param message
     * @param okListener
     */
    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(currentActivity)
            .setMessage(message)
            .setPositiveButton("Ok", okListener)
            .setNegativeButton("Cancel", okListener)
            .create()
            .show()
    }

    companion object {
        fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            permission!!
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return false
                    }
                }
            }
            return true
        }
    }
}
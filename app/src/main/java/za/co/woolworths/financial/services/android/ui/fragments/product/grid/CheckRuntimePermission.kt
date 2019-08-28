package za.co.woolworths.financial.services.android.ui.fragments.product.grid

import android.Manifest
import android.app.Activity
import za.co.woolworths.financial.services.android.util.PermissionResultCallback
import za.co.woolworths.financial.services.android.util.PermissionUtils
import java.util.ArrayList

class CheckRuntimePermission(private val activity: Activity?) {

    private var permissions: MutableList<String>? = null
    private var permissionUtils: PermissionUtils? = null


    private fun initPermissionCheck() {
        permissions = mutableListOf()
        activity?.apply {
            permissionUtils = PermissionUtils(this, object : PermissionResultCallback {
                override fun PermissionGranted(request_code: Int) {

                }

                override fun PartialPermissionGranted(request_code: Int, granted_permissions: ArrayList<String>?) {
                }

                override fun PermissionDenied(request_code: Int) {
                }

                override fun NeverAskAgain(request_code: Int) {
                }
            })
        }
    }

    fun checkRunTimePermissionForLocation(): Boolean {
        permissionUtils = PermissionUtils(activity, object : PermissionResultCallback {
            override fun PermissionGranted(request_code: Int) {

            }

            override fun PartialPermissionGranted(request_code: Int, granted_permissions: ArrayList<String>?) {
            }

            override fun PermissionDenied(request_code: Int) {
            }

            override fun NeverAskAgain(request_code: Int) {
            }
        })
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionUtils?.checkAndRequestPermissions(permissions, 1) ?: false
    }
}
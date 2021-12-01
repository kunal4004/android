package za.co.woolworths.financial.services.android.ui.vto.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

object PermissionUtil {

    fun hasStoragePermission(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun requestStoragePermission(fragment: Fragment, requestCode: Int) {
        fragment.requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            requestCode
        )
    }

    fun hasMediaLocationPermission(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.ACCESS_MEDIA_LOCATION)
    }

    fun requestMediaLocationPermission(activity: Activity, requestCode: Int) {
        requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_MEDIA_LOCATION),
            requestCode
        )
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions(activity: Activity, list: Array<String>, code: Int) {
        ActivityCompat.requestPermissions(activity, list, code)
    }

}
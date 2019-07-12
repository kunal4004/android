package za.co.woolworths.financial.services.android.util

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment

class DialogManager constructor(private val activity: Activity) {

    fun showBasicDialog(description: String) {
        activity.let { act ->
            val appCompatActivity: AppCompatActivity = act as AppCompatActivity
            val fm = appCompatActivity.supportFragmentManager
            val singleButtonDialogFragment = SingleButtonDialogFragment.newInstance(description)
            singleButtonDialogFragment.show(fm, SingleButtonDialogFragment::class.java.simpleName)
        }
    }
}
package za.co.woolworths.financial.services.android.ui.fragments.account.main.core

import android.app.Activity
import android.view.Gravity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.views.alert.Alerter

object ToastFactory  {

    private const val toastDurationInMilliSeconds = 3000

    fun showNoConnectionFound(activity: Activity?) {
        activity ?: return
        Alerter.create(activity)
            .setTitle("")
            .setText(activity.resources.getString(R.string.no_connection))
            .setContentGravity(Gravity.CENTER)
            .setBackgroundColor(R.color.header_red)
            .setDuration(toastDurationInMilliSeconds.toLong())
            .show()
    }

}
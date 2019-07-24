package za.co.absa.openbankingapi.woolworths.integration.service

import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment

open class VolleyErrorHandler(private val activity: AppCompatActivity?, private val volleyError: VolleyError) {
    fun show() {
        when (volleyError) {
            is NoConnectionError, is TimeoutError -> showErrorDialog(R.string.check_connection_status)
            is ServerError, is NetworkError -> showErrorDialog(R.string.general_error_desc)
            else -> showErrorDialog(volleyError.message
                    ?: activity?.resources?.getString(R.string.general_error_desc))
        }
    }

    private fun showErrorDialog(id: Int) {
        activity?.apply {
            supportFragmentManager?.apply {
                val singleButtonDialogFragment = SingleButtonDialogFragment.newInstance(getString(id))
                singleButtonDialogFragment.show(this, SingleButtonDialogFragment::class.java.simpleName)
            }
        }
    }

    private fun showErrorDialog(message: String?) {
        activity?.apply {
            supportFragmentManager?.apply {
                val singleButtonDialogFragment = SingleButtonDialogFragment.newInstance(message)
                singleButtonDialogFragment.show(this, SingleButtonDialogFragment::class.java.simpleName)
            }
        }
    }
}
package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.error_message_fragment.*
import za.co.woolworths.financial.services.android.util.KotlinUtils

class EnableLocationSettingsFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    companion object {
        const val ACCESS_MY_LOCATION_REQUEST_CODE = 1200
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.enable_location_settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnDismissDialog?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        KotlinUtils.openAccessMyLocationDeviceSettings(ACCESS_MY_LOCATION_REQUEST_CODE, activity)
        dismissAllowingStateLoss()
    }
}

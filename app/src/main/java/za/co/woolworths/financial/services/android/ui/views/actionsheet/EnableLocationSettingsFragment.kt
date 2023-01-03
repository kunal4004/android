package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.EnableLocationSettingsFragmentBinding
import za.co.woolworths.financial.services.android.util.KotlinUtils

class EnableLocationSettingsFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    companion object {
        const val ACCESS_MY_LOCATION_REQUEST_CODE = 1200
    }

    private lateinit var binding: EnableLocationSettingsFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = EnableLocationSettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnDismissDialog?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        KotlinUtils.openAccessMyLocationDeviceSettings(ACCESS_MY_LOCATION_REQUEST_CODE, activity)
        dismissAllowingStateLoss()
    }
}

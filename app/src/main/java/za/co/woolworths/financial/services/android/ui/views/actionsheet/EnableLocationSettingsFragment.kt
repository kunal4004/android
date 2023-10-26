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

    private var _binding: EnableLocationSettingsFragmentBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = EnableLocationSettingsFragmentBinding.inflate(inflater, container, false)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

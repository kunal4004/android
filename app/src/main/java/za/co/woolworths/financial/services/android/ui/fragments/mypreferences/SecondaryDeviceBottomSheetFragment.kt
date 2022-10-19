package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentSecondaryDeviceBottomSheetBinding
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class SecondaryDeviceBottomSheetFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentSecondaryDeviceBottomSheetBinding
    private var newPrimaryDevice: UserDevice? = null
    private var oldPrimaryDevice: UserDevice? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentSecondaryDeviceBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            arguments?.apply {
                newPrimaryDevice =
                    getSerializable(ViewAllLinkedDevicesFragment.NEW_DEVICE) as UserDevice?
                oldPrimaryDevice =
                    getSerializable(ViewAllLinkedDevicesFragment.OLD_DEVICE) as UserDevice?
                unlinkDeviceTitle.text = newPrimaryDevice?.deviceName
            }

            unlinkDeviceCancel.setOnClickListener {
                dismissAllowingStateLoss()
                AnimationUtilExtension.animateViewPushDown(it)
            }
            changePrimaryDeviceLayout.setOnClickListener(this@SecondaryDeviceBottomSheetFragment)
            deleteDeviceLayout.setOnClickListener(this@SecondaryDeviceBottomSheetFragment)
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.deleteDeviceLayout -> {
                dismissAllowingStateLoss()
                AnimationUtilExtension.animateViewPushDown(v)

                setFragmentResult(ViewAllLinkedDevicesFragment.CONFIRM_DELETE_SECONDARY_DEVICE, Bundle.EMPTY)

            }
            R.id.changePrimaryDeviceLayout -> {
                val bundle = Bundle()
                bundle.putSerializable(ViewAllLinkedDevicesFragment.NEW_DEVICE, newPrimaryDevice)
                bundle.putSerializable(ViewAllLinkedDevicesFragment.OLD_DEVICE, oldPrimaryDevice)
                setFragmentResult(ViewAllLinkedDevicesFragment.CHANGE_PRIMARY_DEVICE_OTP, bundle)
                dismissAllowingStateLoss()
                AnimationUtilExtension.animateViewPushDown(v)
            }
        }
    }


}
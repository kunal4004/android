package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_unlink_primary_device_bottom_sheet.*
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesActivity
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class UnlinkPrimaryDeviceBottomSheetFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private var deviceList: ArrayList<UserDevice>? = ArrayList(0)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_unlink_primary_device_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getSerializable(ViewAllLinkedDevicesFragment.DEVICE_LIST)?.let { list ->
            if (list is ArrayList<*> && list[0] is UserDevice) {
                deviceList = list as ArrayList<UserDevice>
            }
        }

        unlinkDeviceCancel.setOnClickListener {
            dismissAllowingStateLoss()
            AnimationUtilExtension.animateViewPushDown(it)
        }
        choosePrimaryDeviceContinue.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.choosePrimaryDeviceContinue -> {
                if(hasNoOtherDevices() == true) {
                    setFragmentResult(ViewAllLinkedDevicesFragment.DELETE_DEVICE_NO_OTP, bundleOf(
                        ViewAllLinkedDevicesFragment.KEY_BOOLEAN_UNLINK_DEVICE to true
                    ))
                } else {
                    setFragmentResult(ViewAllLinkedDevicesFragment.CHOOSE_PRIMARY_DEVICE_FRAGMENT, Bundle.EMPTY)
                }
                dismissAllowingStateLoss()
                AnimationUtilExtension.animateViewPushDown(v)
            }
        }
    }

    private fun hasNoOtherDevices(): Boolean? {
        val defaultPrimaryDeviceList = deviceList?.filter { userDevice -> userDevice.primarydDevice == true }
        if (defaultPrimaryDeviceList != null) {
            if(defaultPrimaryDeviceList.isNotEmpty()){
                (activity as? MyPreferencesActivity)?.setDefaultPrimaryDevice(
                    defaultPrimaryDeviceList[0]
                )
            }
        }

        return deviceList?.none { userDevice -> userDevice.primarydDevice == false }
    }
}
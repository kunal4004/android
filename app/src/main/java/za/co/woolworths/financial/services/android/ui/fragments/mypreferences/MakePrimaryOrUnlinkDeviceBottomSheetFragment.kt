package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_make_primary_or_unlink_device_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_unlink_primary_device_bottom_sheet.unlinkDeviceCancel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class MakePrimaryOrUnlinkDeviceBottomSheetFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_make_primary_or_unlink_device_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.apply {
            unlinkDeviceTitle.text = getString(ViewAllLinkedDevicesFragment.DEVICE_NAME, null)
        }

        unlinkDeviceCancel.setOnClickListener {
            dismissAllowingStateLoss()
            AnimationUtilExtension.animateViewPushDown(it)
        }
        changePrimaryDeviceLayout.setOnClickListener(this)
        deleteDeviceLayout.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.deleteDeviceLayout -> {
                setFragmentResult(ViewAllLinkedDevicesFragment.DELETE_DEVICE_NO_OTP, bundleOf(
                        ViewAllLinkedDevicesFragment.KEY_BOOLEAN_UNLINK_DEVICE to true
                ))
                dismissAllowingStateLoss()
                AnimationUtilExtension.animateViewPushDown(v)
            }
            R.id.changePrimaryDeviceLayout -> {
                //Do OTP to add this chosen device as primary device
                System.err.println("TEST: changePrimaryDeviceLayout ")
                dismissAllowingStateLoss()
                AnimationUtilExtension.animateViewPushDown(v)
            }
        }
    }


}
package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_unlink_device_bottom_sheet.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class UnlinkDeviceBottomSheetFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_unlink_device_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        unlinkDeviceCancel.setOnClickListener {
            dismissAllowingStateLoss()
            AnimationUtilExtension.animateViewPushDown(it)
        }
        unlinkDeviceContinue.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.unlinkDeviceContinue -> {

                setFragmentResult(ViewAllLinkedDevicesFragment.DELETE_DEVICE, bundleOf(
                        ViewAllLinkedDevicesFragment.KEY_BOOLEAN_UNLINK_DEVICE to true
                ))
                dismissAllowingStateLoss()
                AnimationUtilExtension.animateViewPushDown(v)
            }
        }
    }


}
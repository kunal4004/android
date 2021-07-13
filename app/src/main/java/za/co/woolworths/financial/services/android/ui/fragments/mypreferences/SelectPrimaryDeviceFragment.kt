package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_select_primary_device.*
import kotlinx.android.synthetic.main.item_select_primary_device_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.adapters.SelectPrimaryDeviceAdapter
import za.co.woolworths.financial.services.android.ui.extension.setDivider

class SelectPrimaryDeviceFragment : Fragment(), View.OnClickListener {

    private var selectPrimaryDeviceAdapter: SelectPrimaryDeviceAdapter? = null
    private var deviceList: ArrayList<UserDevice> = ArrayList(0)
    private var deviceSelected: UserDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getSerializable(ViewAllLinkedDevicesFragment.DEVICE_LIST)?.let { list ->
            if (list is ArrayList<*> && list[0] is UserDevice) {
                deviceList = list as ArrayList<UserDevice>
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_primary_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        if (selectPrimaryDeviceAdapter == null) {
            initRecyclerView()
        }
    }

    private fun setupToolbar() {
        activity?.apply {
            when (this) {
                is MyPreferencesInterface -> {
                    context?.let {
                        setToolbarTitle("")
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {

        if (deviceList.isNullOrEmpty()) {
            return
        }
        context?.let {
            selectPrimaryDeviceRecyclerView.layoutManager = LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            selectPrimaryDeviceRecyclerView.setDivider(R.drawable.recycler_view_divider_light_gray_1dp)
            selectPrimaryDeviceAdapter = SelectPrimaryDeviceAdapter(
                deviceList.filter { it.primarydDevice != true } as ArrayList<UserDevice>,
                this)
        }
        selectPrimaryDeviceRecyclerView.adapter = selectPrimaryDeviceAdapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.selectPrimaryDeviceConstraintLayout -> {
                v.deviceRadioButton.isChecked = !v.deviceRadioButton.isChecked
                changePrimaryDeviceButton.isEnabled = !changePrimaryDeviceButton.isEnabled

                if(v.deviceRadioButton.isChecked) {
                    deviceSelected = v.getTag(R.id.selectPrimaryDeviceConstraintLayout) as UserDevice
                }
            }
            R.id.changePrimaryDeviceButton -> {
                setFragmentResult(ViewAllLinkedDevicesFragment.CHANGE_TO_PRIMARY_DEVICE_OTP,
                    bundleOf(ViewAllLinkedDevicesFragment.PRIMARY_DEVICE to deviceSelected))
            }
        }
    }
}
package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
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
        initRecyclerView()

        changePrimaryDeviceButton?.setOnClickListener(this)
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
            resetDevicesIfBackPressed()
            selectPrimaryDeviceRecyclerView.layoutManager = LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            selectPrimaryDeviceRecyclerView.setDivider(R.drawable.recycler_view_divider_light_gray_1dp)
            selectPrimaryDeviceAdapter = SelectPrimaryDeviceAdapter(
                deviceList,
                this)
        }
        selectPrimaryDeviceRecyclerView.adapter = selectPrimaryDeviceAdapter
    }

    private fun resetDevicesIfBackPressed() {
        deviceList.forEach { userDevice ->
            userDevice.primarydDevice = false
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.selectPrimaryDeviceConstraintLayout -> {
                selectPrimaryDeviceAdapter?.deviceList?.forEach {
                    it.primarydDevice = false
                }
                v.deviceRadioButton.isChecked = !v.deviceRadioButton.isChecked
                changePrimaryDeviceButton.isEnabled = v.deviceRadioButton.isChecked
                val position = v.getTag(R.id.selectPrimaryDeviceConstraintLayout) as Int
                selectPrimaryDeviceAdapter?.deviceList?.get(position)?.primarydDevice = v.deviceRadioButton.isChecked
                selectPrimaryDeviceAdapter?.notifyDataSetChanged()

                deviceSelected = if(changePrimaryDeviceButton.isEnabled)
                    selectPrimaryDeviceAdapter?.deviceList?.get(position) else null

            }
            R.id.changePrimaryDeviceButton -> {
                val bundle = Bundle()
                bundle.putSerializable(ViewAllLinkedDevicesFragment.NEW_DEVICE, deviceSelected)
                bundle.putSerializable(ViewAllLinkedDevicesFragment.OLD_DEVICE,
                    deviceList.filter { device -> device.primarydDevice == true }[0]
                )
                bundle.putBoolean(ViewAllLinkedDevicesFragment.DELETE_PRIMARY_DEVICE, true)
                val navController = view?.findNavController()
                navController?.navigate(R.id.action_link_new_primary_device, bundle)
            }
        }
    }
}
package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentSelectPrimaryDeviceBinding
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.adapters.SelectPrimaryDeviceAdapter
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class SelectPrimaryDeviceFragment : BaseFragmentBinding<FragmentSelectPrimaryDeviceBinding>(FragmentSelectPrimaryDeviceBinding::inflate),
    SelectPrimaryDeviceAdapter.ISelectPrimaryDeviceClickListener, View.OnClickListener {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        initRecyclerView()

        binding.changePrimaryDeviceButton?.setOnClickListener(this)
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
            binding.selectPrimaryDeviceRecyclerView.layoutManager = LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            selectPrimaryDeviceAdapter = SelectPrimaryDeviceAdapter(
                deviceList,
                this)
        }
        binding.selectPrimaryDeviceRecyclerView.adapter = selectPrimaryDeviceAdapter
    }

    private fun resetDevicesIfBackPressed() {
        deviceList.forEach { userDevice ->
            userDevice.primarydDevice = false
        }
    }

    override fun onPrimaryDeviceClicked(position: Int, isChecked: Boolean) {
        selectPrimaryDeviceAdapter?.deviceList?.forEach {
            it.primarydDevice = false
        }
        binding.changePrimaryDeviceButton.isEnabled = isChecked
        selectPrimaryDeviceAdapter?.deviceList?.get(position)?.primarydDevice = isChecked
        selectPrimaryDeviceAdapter?.notifyDataSetChanged()

        deviceSelected = if(binding.changePrimaryDeviceButton.isEnabled)
            selectPrimaryDeviceAdapter?.deviceList?.get(position) else null
    }

    override fun onClick(v: View?) {
        when (v?.id) {
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
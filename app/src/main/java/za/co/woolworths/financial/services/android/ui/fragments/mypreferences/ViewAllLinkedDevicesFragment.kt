package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.android.gms.common.util.ArrayUtils
import kotlinx.android.synthetic.main.fragment_view_all_linked_devices.*
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationInterface
import za.co.woolworths.financial.services.android.ui.adapters.ViewAllLinkedDevicesAdapter
import za.co.woolworths.financial.services.android.ui.fragments.mypreferences.MyPreferencesFragment.Companion.DEVICE_LIST

class ViewAllLinkedDevicesFragment : Fragment() {
    private var viewAllDevicesAdapter: ViewAllLinkedDevicesAdapter? = null
    private var deviceList: ArrayList<UserDevice>? = ArrayList(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getSerializable(DEVICE_LIST)?.let { list ->
            if (list is ArrayList<*> && list?.get(0) is UserDevice) {
                deviceList = list as ArrayList<UserDevice>
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_all_linked_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        if (viewAllDevicesAdapter == null) {
            initRecyclerView()
        }
    }

    private fun setupToolbar() {
        activity?.apply {
            when (this) {
                is MyPreferencesInterface -> {
                    context?.let {
                        setToolbarTitle(it.getString(R.string.view_all_device_title))
                        setToolbarTitleGravity(Gravity.CENTER_HORIZONTAL)
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {

        if(deviceList == null || deviceList?.isEmpty() == true){
            return
        }
        context?.let {
            viewAllLinkedDevicesRecyclerView.layoutManager = LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            viewAllDevicesAdapter = ViewAllLinkedDevicesAdapter(it)
            viewAllDevicesAdapter?.setDeviceList(deviceList)
        }
        viewAllLinkedDevicesRecyclerView.adapter = viewAllDevicesAdapter
    }

    companion object {
        const val DEVICE_LIST = "deviceList"
    }
}
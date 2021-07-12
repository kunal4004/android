package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_select_primary_device.*
import kotlinx.android.synthetic.main.fragment_select_primary_device.view.*
import kotlinx.android.synthetic.main.fragment_view_all_linked_devices.progressLoadDevices
import kotlinx.android.synthetic.main.item_select_primary_device_layout.view.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.adapters.SelectPrimaryDeviceAdapter
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils

class SelectPrimaryDeviceFragment : Fragment(), View.OnClickListener {

    private var deviceIdentityId: String = ""
    private var unlinkOrDeleteDeviceReq: Call<ViewAllLinkedDeviceResponse>? = null
    private var selectPrimaryDeviceAdapter: SelectPrimaryDeviceAdapter? = null
    private var deviceList: ArrayList<UserDevice> = ArrayList(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getSerializable(DEVICE_LIST)?.let { list ->
            if (list is ArrayList<*> && list[0] is UserDevice) {
                deviceList = list as ArrayList<UserDevice>
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setFragmentResultListener(DELETE_DEVICE) { requestKey, bundle ->

            val isUnlinkSuccess = bundle.getBoolean(KEY_BOOLEAN_UNLINK_DEVICE)
            if (isUnlinkSuccess) {
                unlinkDevice()
            }
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_primary_device, container, false)
    }

    private fun unlinkDevice() {
        unlinkOrDeleteDeviceReq = OneAppService.deleteOrUnlinkDevice(deviceIdentityId)
        unlinkOrDeleteDeviceReq?.enqueue(CompletionHandler(
                object : IResponseListener<ViewAllLinkedDeviceResponse> {
                    override fun onSuccess(response: ViewAllLinkedDeviceResponse?) {

                        when (response?.httpCode) {
                            AppConstant.HTTP_OK -> {
                                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.DEVICESECURITY_DELETE, hashMapOf(Pair(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE, FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceDelete)))

                                setFragmentResult(MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, bundleOf(
                                        "isUpdate" to true
                                ))

                                deviceList = response.userDevices!!
                                if (deviceList.isNullOrEmpty()) {
                                    view?.findNavController()?.navigateUp()
                                    return
                                }

                                initRecyclerView()
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        super.onFailure(error)
                    }
                }, ViewAllLinkedDeviceResponse::
        class.java))
    }

    private fun callRetrieveDevices() {
        progressLoadDevices.visibility = View.VISIBLE
        val mViewAllLinkedDevices: Call<ViewAllLinkedDeviceResponse> = OneAppService.getAllLinkedDevices(true)
        mViewAllLinkedDevices.enqueue(CompletionHandler(object : IResponseListener<ViewAllLinkedDeviceResponse> {
            override fun onFailure(error: Throwable?) {
                //Do Nothing
                progressLoadDevices.visibility = View.GONE
            }

            override fun onSuccess(response: ViewAllLinkedDeviceResponse?) {
                progressLoadDevices.visibility = View.GONE
                deviceList = ArrayList(0)
                deviceList = response?.userDevices!!
                if (deviceList.isNullOrEmpty()) {
                    setFragmentResult(MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, bundleOf(
                            "isUpdate" to true
                    ))
                    view?.findNavController()?.navigateUp()
                    return
                }
                initRecyclerView()
            }
        }, ViewAllLinkedDeviceResponse::class.java))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        callRetrieveDevices()
        if (selectPrimaryDeviceAdapter == null) {
            initRecyclerView()
        }
    }

    private fun setupToolbar() {
        activity?.apply {
            when (this) {
                is MyPreferencesInterface -> {
                    context?.let {
                        setToolbarTitle(it.getString(R.string.select_primary_device_title))
                        setToolbarTitleGravity(Gravity.CENTER_HORIZONTAL)
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
            selectPrimaryDeviceAdapter = SelectPrimaryDeviceAdapter(
                it,
                deviceList.filter { it.primarydDevice != true } as ArrayList<UserDevice>,
                this)
        }
        selectPrimaryDeviceRecyclerView.adapter = selectPrimaryDeviceAdapter
    }

    companion object {
        const val DEVICE_LIST = "deviceList"
        const val DELETE_DEVICE = "deleteDevice"
        const val KEY_BOOLEAN_UNLINK_DEVICE = "isUnlinkSuccess"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.selectPrimaryDeviceConstraintLayout -> {
                v.deviceCheckbox.isChecked = !v.deviceCheckbox.isChecked
                v.changePrimaryDeviceButton.isEnabled = !v.changePrimaryDeviceButton.isEnabled

                if(v.deviceCheckbox.isChecked) {
                    val userSelectedDevice = v.getTag(R.id.selectPrimaryDeviceConstraintLayout) as UserDevice
                }
            }
            R.id.changePrimaryDeviceButton -> {
                val userSelectedDevice = v.getTag(R.id.selectPrimaryDeviceConstraintLayout) as UserDevice
            }
        }
    }
}
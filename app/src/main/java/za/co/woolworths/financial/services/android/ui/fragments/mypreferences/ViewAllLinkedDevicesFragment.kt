package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_view_all_linked_devices.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.DeleteMessageResponse
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.adapters.ViewAllLinkedDevicesAdapter
import za.co.woolworths.financial.services.android.util.AppConstant

class ViewAllLinkedDevicesFragment : Fragment(), View.OnClickListener {

    private var deviceIdentityId: String = ""
    private var unlinkOrDeleteDeviceReq: Call<DeleteMessageResponse>? = null
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
        setFragmentResultListener(DELETE_DEVICE) { requestKey, bundle ->

            val isUnlinkSuccess = bundle.getBoolean(KEY_BOOLEAN_UNLINK_DEVICE)
            if (isUnlinkSuccess) {
                unlinkDevice()
            }

        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_all_linked_devices, container, false)
    }

    private fun unlinkDevice() {
        unlinkOrDeleteDeviceReq = OneAppService.deleteOrUnlinkDevice(deviceIdentityId)
        unlinkOrDeleteDeviceReq?.enqueue(CompletionHandler(
                object : IResponseListener<DeleteMessageResponse> {
                    override fun onSuccess(response: DeleteMessageResponse?) {

                        when (response?.httpCode) {
                            AppConstant.HTTP_OK -> {
                                callRetrieveDevices()
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        super.onFailure(error)
                    }
                }, DeleteMessageResponse::
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
                deviceList = response?.userDevices
                initRecyclerView()
            }
        }, ViewAllLinkedDeviceResponse::class.java))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        callRetrieveDevices()
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

        if (deviceList.isNullOrEmpty()) {
            return
        }
        context?.let {
            viewAllLinkedDevicesRecyclerView.layoutManager = LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            viewAllDevicesAdapter = ViewAllLinkedDevicesAdapter(it, this)
            viewAllDevicesAdapter?.setDeviceList(deviceList)
        }
        viewAllLinkedDevicesRecyclerView.adapter = viewAllDevicesAdapter
    }

    companion object {
        const val DEVICE_LIST = "deviceList"
        const val DELETE_DEVICE = "deleteDevice"
        const val KEY_BOOLEAN_UNLINK_DEVICE = "isUnlinkSuccess"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.viewAllDeviceDeleteImageView -> {

                val userDevice = v?.getTag(R.id.viewAllDeviceDeleteImageView) as UserDevice
                if (TextUtils.isEmpty(userDevice?.deviceIdentityId.toString())) {
                    return
                }

                deviceIdentityId = userDevice?.deviceIdentityId?.toString() ?: ""
                val navController = view?.findNavController()
                navController?.navigate(R.id.action_viewAllLinkedDevicesFragment_to_unlinkDeviceBottomSheetFragment)
            }
        }
    }
}
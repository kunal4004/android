package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentViewAllLinkedDevicesBinding
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.adapters.ViewAllLinkedDevicesAdapter
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class ViewAllLinkedDevicesFragment : Fragment(R.layout.fragment_view_all_linked_devices), View.OnClickListener {

    private lateinit var binding: FragmentViewAllLinkedDevicesBinding
    private var deviceIdentityId: String = ""
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

    private fun unlinkDevice() {
        OneAppService().deleteDevice(deviceIdentityId, null, null, null)
            .enqueue(CompletionHandler(
                object : IResponseListener<ViewAllLinkedDeviceResponse> {
                    override fun onSuccess(response: ViewAllLinkedDeviceResponse?) {

                        when (response?.httpCode) {
                            AppConstant.HTTP_OK -> {
                                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.DEVICESECURITY_DELETE, hashMapOf(Pair(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE, FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceDelete)), this) }

                                deviceList = response.userDevices

                                binding.showDeviceUnlinked()

                                Handler().postDelayed({
                                    if (!isAdded) return@postDelayed

                                    setupToolbar()
                                    context?.let { it ->
                                        binding.viewAllDeviceConstraintLayout?.background = AppCompatResources.getDrawable(it, R.color.default_background)
                                    }
                                    binding.unlinkDeviceConfirmationConstraintLayout?.root?.visibility = View.GONE
                                    binding.viewAllLinkedDevicesRecyclerView?.visibility = View.VISIBLE

                                    setFragmentResult(MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, bundleOf(
                                        IS_UPDATE to true
                                    ))
                                    if (deviceList.isNullOrEmpty()) {
                                        SessionUtilities.getInstance().removeCurrentDeviceIdentityToken()
                                        view?.findNavController()?.navigateUp()
                                        return@postDelayed
                                    }

                                    binding.initRecyclerView()
                                }, AppConstant.DELAY_1000_MS)
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        binding.apply {
                            progressLoadDevices?.visibility = View.GONE
                        }
                    }
                }, ViewAllLinkedDeviceResponse::class.java))
    }

    private fun FragmentViewAllLinkedDevicesBinding.callRetrieveDevices() {
        progressLoadDevices?.visibility = View.VISIBLE
        val mViewAllLinkedDevices: Call<ViewAllLinkedDeviceResponse> = OneAppService().getAllLinkedDevices(true)
        mViewAllLinkedDevices.enqueue(CompletionHandler(object : IResponseListener<ViewAllLinkedDeviceResponse> {
            override fun onFailure(error: Throwable?) {
                //Do Nothing
                progressLoadDevices?.visibility = View.GONE
            }
            
            override fun onSuccess(response: ViewAllLinkedDeviceResponse?) {
                progressLoadDevices?.visibility = View.GONE
                deviceList = ArrayList(0)
                deviceList = response?.userDevices
                if (deviceList.isNullOrEmpty()) {
                    setFragmentResult(MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, bundleOf(
                            IS_UPDATE to true
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
        binding = FragmentViewAllLinkedDevicesBinding.bind(view)

        setFragmentResultListener(CONFIRM_DELETE_SECONDARY_DEVICE) { requestKey, bundle ->
            val navController = view?.findNavController()
            navController?.navigate(R.id.action_viewAllLinkedDevicesFragment_to_deletePrimaryDeviceFragment, bundleOf(
                DEVICE_LIST to null
            ))
        }

        setFragmentResultListener(DELETE_DEVICE_NO_OTP) { requestKey, bundle ->
            val isUnlinkSuccess = bundle.getBoolean(KEY_BOOLEAN_UNLINK_DEVICE)
            if (isUnlinkSuccess) {
                unlinkDevice()
            }
        }

        setFragmentResultListener(CHOOSE_PRIMARY_DEVICE_FRAGMENT) { requestKey, bundle ->
            val navController = view?.findNavController()
            navController?.navigate(R.id.action_to_selectPrimaryDeviceFragment,
                bundleOf(
                    DEVICE_LIST to deviceList?.filter { it.primarydDevice != true }
                ))
        }

        setFragmentResultListener(CHANGE_PRIMARY_DEVICE_OTP) { requestKey, bundle ->
            val navController = view?.findNavController()
            bundle.putBoolean(DELETE_PRIMARY_DEVICE, false)
            navController?.navigate(R.id.action_change_to_primary_device, bundle)
        }

        setupToolbar()
        binding.callRetrieveDevices()
        if (viewAllDevicesAdapter == null) {
            binding.initRecyclerView()
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

    private fun clearToolbar() {
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

    private fun FragmentViewAllLinkedDevicesBinding.initRecyclerView() {

        if (deviceList.isNullOrEmpty()) {
            return
        }
        context?.let {
            viewAllLinkedDevicesRecyclerView?.layoutManager = LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            viewAllDevicesAdapter = ViewAllLinkedDevicesAdapter(it, this@ViewAllLinkedDevicesFragment)
            deviceList?.sortByDescending { userDevice -> userDevice.primarydDevice }
            viewAllDevicesAdapter?.setDeviceList(deviceList)
        }
        viewAllLinkedDevicesRecyclerView?.adapter = viewAllDevicesAdapter
    }

    private fun FragmentViewAllLinkedDevicesBinding.showDeviceUnlinked() {
        clearToolbar()
        viewAllLinkedDevicesRecyclerView?.visibility = View.GONE
        unlinkDeviceConfirmationConstraintLayout?.root?.visibility = View.VISIBLE
        unlinkDeviceConfirmationConstraintLayout.unlinkDeviceResultSubtitle?.visibility = View.GONE
        context?.let { it ->
            viewAllDeviceConstraintLayout?.background = AppCompatResources.getDrawable(it, R.color.white)
            unlinkDeviceConfirmationConstraintLayout.unlinkDeviceResultTitle?.text = it.getString(R.string.unlink_device_result_success)
        }
    }

    companion object {
        const val DEVICE_LIST = "deviceList"
        const val DELETE_DEVICE_NO_OTP = "deleteDevice"
        const val KEY_BOOLEAN_UNLINK_DEVICE = "isUnlinkSuccess"
        const val CHOOSE_PRIMARY_DEVICE_FRAGMENT = "choosePrimaryDeviceFragment"
        const val DEVICE = "device"
        const val NEW_DEVICE = "newPrimaryDevice"
        const val OLD_DEVICE = "oldPrimaryDevice"
        const val CHANGE_PRIMARY_DEVICE_OTP = "changePrimaryDevice"
        const val DELETE_PRIMARY_DEVICE = "deleteOldPrimaryDevice"
        const val IS_UPDATE = "isUpdate"
        const val CONFIRM_DELETE_SECONDARY_DEVICE = "CONFIRM_DELETE_SECONDARY_DEVICE"
    }

    override fun onClick(v: View?) {

        val navController = view?.findNavController()
        val userDevice = v?.getTag(v.id) as UserDevice

        if (TextUtils.isEmpty(userDevice.deviceIdentityId.toString())) {
            return
        }
        deviceIdentityId = userDevice.deviceIdentityId?.toString() ?: ""

        when (v.id) {
            R.id.viewAllDeviceDeleteImageView -> {
                try{
                    navController?.navigate(R.id.action_viewAllLinkedDevicesFragment_to_deletePrimaryDeviceFragment, bundleOf(
                        DEVICE_LIST to deviceList
                    ))
                } catch (e: Exception) {
                    FirebaseManager.logException(e)
                }
            }
            R.id.viewAllDeviceEditImageView -> {
                val bundle = Bundle()
                bundle.putSerializable(NEW_DEVICE, userDevice)
                deviceList?.firstOrNull { device -> device.primarydDevice == true }?.let {
                    bundle.putSerializable(OLD_DEVICE, it)
                }
                navController?.navigate(R.id.action_viewAllLinkedDevicesFragment_to_secondaryDeviceBottomSheetFragment, bundle)
            }
        }
    }
}
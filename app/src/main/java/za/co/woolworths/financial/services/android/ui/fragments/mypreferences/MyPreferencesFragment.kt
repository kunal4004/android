package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentMyPreferencesBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.DeleteAccountResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.repository.AppStateRepository
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.shop.StandardDeliveryFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.AuthenticateUtils
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELETE_ACCOUNT
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELETE_ACCOUNT_CONFIRMATION
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.RESULT_CODE_DELETE_ACCOUNT
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.REQUEST_CODE
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.setDeliveryAddressView
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import com.awfs.coordination.BuildConfig
import javax.inject.Inject

@AndroidEntryPoint
class MyPreferencesFragment : BaseFragmentBinding<FragmentMyPreferencesBinding>(FragmentMyPreferencesBinding::inflate), View.OnClickListener,
    VtoTryAgainListener {

    private lateinit var locator: Locator
    private var isNonWFSUser: Boolean = true
    private var mViewAllLinkedDevices: Call<ViewAllLinkedDeviceResponse>? = null
    private var deviceList: ArrayList<UserDevice>? = ArrayList(0)
    private var isUpdateAccountCache: Boolean = false
    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog
    private var deleteAccountApi: Call<DeleteAccountResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            isNonWFSUser = getBoolean(IS_NON_WFS_USER)
            val list = getSerializable(DEVICE_LIST)
            if (list is Array<*> && list.isNotEmpty()) {
                list.forEach {
                    deviceList?.add(it as UserDevice)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
        activity?.runOnUiThread { activity?.window?.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN) }

        setFragmentResultListener(RESULT_LISTENER_LINK_DEVICE) { requestKey, bundle ->
            Utils.setLinkConfirmationShown(true)
            isUpdateAccountCache = true
            callLinkedDevicesAPI()
        }

        init()
        bindDataWithUI()
    }


    private fun init() {
        locator = Locator(activity as AppCompatActivity)

        binding.apply {
            biometricAuthenticationSwitchCompat.setOnClickListener(this@MyPreferencesFragment)
            locationSelectedLayout.setOnClickListener(this@MyPreferencesFragment)
            linkDeviceSwitch.setOnClickListener(this@MyPreferencesFragment)
            retryLinkDeviceLinearLayout?.setOnClickListener(this@MyPreferencesFragment)
            viewAllLinkedDevicesRelativeLayout?.setOnClickListener(this@MyPreferencesFragment)
            deleteAccountLayout?.setOnClickListener(this@MyPreferencesFragment)
            showHuaweiToken()
        }

        activity?.apply {
            if (this is MyPreferencesInterface) {
                setToolbarTitle(getString(R.string.acc_my_preferences))
                setToolbarTitleGravity(Gravity.START)
            }
        }
        setFragmentResultListener(DELETE_ACCOUNT_CONFIRMATION) { _, bundle ->
            if (bundle.getString(DELETE_ACCOUNT) == DELETE_ACCOUNT) {
                callDeleteAccountApi()
            }
        }
    }

    private fun callDeleteAccountApi() {
        showProgress()
        deleteAccountApi = OneAppService().deleteAccount()
        deleteAccountApi?.enqueue(CompletionHandler(object :
            IResponseListener<DeleteAccountResponse> {

            override fun onSuccess(response: DeleteAccountResponse?) {
                if (response?.httpCode == HTTP_OK) {
                    hideProgress()
                    activity?.setResult(RESULT_CODE_DELETE_ACCOUNT)
                    activity?.finish()
                } else {
                    hideProgress()
                    showErrorDialog()
                }
            }

            override fun onFailure(error: Throwable?) {
                hideProgress()
                showErrorDialog()
            }

        }, DeleteAccountResponse::class.java))
    }

    private fun hideProgress() {
        binding.apply {
            incCenteredProgress?.root?.visibility = View.GONE
            myPreProgressLayout?.isClickable = false
            myPreProgressLayout?.isFocusable = false
        }
    }

    private fun showProgress() {
        binding.apply {
            incCenteredProgress?.root?.visibility = View.VISIBLE
            myPreProgressLayout?.isClickable = true
            myPreProgressLayout?.isFocusable = true
        }
    }

    fun bindDataWithUI() {
        binding.apply {
                if (AuthenticateUtils.isDeviceSecure(requireContext())) biometricAuthenticationSwitchCompat.isChecked =
                    AuthenticateUtils.isAuthenticationEnabled() else setUserAuthentication(
                    false
                )
            val lastDeliveryLocation = Utils.getPreferredDeliveryLocation()
            lastDeliveryLocation?.let { setDeliveryLocation(it) }

            if (Utils.isGooglePlayOrHuaweiMobileServicesAvailable()) {
                val isDeviceIdentityIdPresent = verifyDeviceIdentityId(deviceList)
                updateLinkedDeviceView(isDeviceIdentityIdPresent)
            }
            tvMyPrefManageDevicesTitle.text =
                bindString(
                    R.string.my_preferences_linked_devices,
                    (deviceList?.size ?: 0).toString()
                )
        }
    }

    private fun callLinkedDevicesAPI() {
        binding.apply {
            val spinningAnimation = KotlinUtils.rotateViewAnimation()
            retryLinkDeviceImageView?.startAnimation(spinningAnimation)
            retryLinkDeviceLinearLayout?.visibility = View.VISIBLE
            retryLinkDeviceTextView?.visibility = View.GONE

            mViewAllLinkedDevices = OneAppService().getAllLinkedDevices(isUpdateAccountCache)
            mViewAllLinkedDevices?.enqueue(CompletionHandler(object :
                IResponseListener<ViewAllLinkedDeviceResponse> {

                override fun onSuccess(response: ViewAllLinkedDeviceResponse?) {
                    when (response?.httpCode) {
                        200 -> {
                            if (!isAdded) {
                                return
                            }
                            spinningAnimation.cancel()
                            retryLinkDeviceLinearLayout?.visibility = View.GONE
                            val isDeviceIdentityIdPresent =
                                verifyDeviceIdentityId(response?.userDevices)
                            deviceList = response?.userDevices
                            AppStateRepository().saveLinkedDevices(deviceList)
                            updateLinkedDeviceView(isDeviceIdentityIdPresent)
                            tvMyPrefManageDevicesTitle.text =
                                bindString(
                                    R.string.my_preferences_linked_devices,
                                    (deviceList?.size ?: 0).toString()
                                )
                        }
                        else -> {
                            spinningAnimation.cancel()
                            showLinkDeviceRetryView()
                        }
                    }
                }

                override fun onFailure(error: Throwable?) {
                    spinningAnimation.cancel()
                    showLinkDeviceRetryView()
                }

            }, ViewAllLinkedDeviceResponse::class.java))
        }
    }

    private fun showLinkDeviceRetryView() {
        binding.apply {
            retryLinkDeviceLinearLayout?.visibility = View.VISIBLE
            retryLinkDeviceTextView?.visibility = View.VISIBLE
            linkDeviceSwitch?.visibility = View.GONE
        }
    }

    private fun updateLinkedDeviceView(deviceIdentityIdPresent: Boolean) {
        binding.apply {
            when (isNonWFSUser) {
                true -> {
                    linkDeviceLayout?.visibility = View.GONE
                }
                else -> {
                    linkDeviceLayout?.visibility = View.VISIBLE
                    linkDeviceSwitch.isChecked = deviceIdentityIdPresent
                    if (deviceList == null || deviceList?.isEmpty() == true) {
                        viewAllLinkedDevicesRelativeLayout.visibility = View.GONE
                    } else {
                        viewAllLinkedDevicesRelativeLayout.visibility = View.VISIBLE
                    }

                    if (deviceIdentityIdPresent) {
                        linkDeviceSwitch.visibility = View.GONE
                        linkDeviceSwitch.isEnabled = false
                        context?.apply {
                            linkThisDeviceTextView?.text =
                                getString(R.string.link_device_this_is_linked)
                        }
                    } else {
                        linkDeviceSwitch.visibility = View.VISIBLE
                        linkDeviceSwitch.isEnabled = true
                        context?.apply {
                            linkThisDeviceTextView?.text =
                                getString(R.string.my_preferences_link_this_device)
                        }
                    }
                }
            }
        }
    }

    private fun verifyDeviceIdentityId(userDevices: ArrayList<UserDevice>?): Boolean {
        var isPresent = false
        when {
            userDevices != null && userDevices.isNotEmpty() -> {
                val appInstanceId = Utils.getUniqueDeviceID()
                userDevices.forEach {
                    if (appInstanceId == it?.appInstanceId) {
                        isPresent = true
                        return@forEach
                    }
                }
            }
            else -> {
                isPresent = false
            }
        }
        return isPresent
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.biometricAuthenticationSwitchCompat -> if (AuthenticateUtils.isDeviceSecure(requireContext())) {
                if (binding.biometricAuthenticationSwitchCompat.isChecked) {
                    startBiometricAuthentication(LOCK_REQUEST_CODE_TO_ENABLE)
                } else {
                    Utils.displayValidationMessageForResult(
                        activity,
                        CustomPopUpWindow.MODAL_LAYOUT.BIOMETRICS_SECURITY_INFO,
                        getString(R.string.biometrics_security_info),
                        SECURITY_INFO_REQUEST_DIALOG
                    )
                }
            } else openDeviceSecuritySettings()
            R.id.locationSelectedLayout -> locationSelectionClicked()
            R.id.deleteAccountLayout -> deleteAccountShowPopup()
            R.id.linkDeviceSwitch -> {
                checkForLocationPermissionAndNavigateToLinkDevice()
            }
            R.id.retryLinkDeviceLinearLayout -> {
                callLinkedDevicesAPI()
            }
            R.id.viewAllLinkedDevicesRelativeLayout -> {
                if (deviceList != null && deviceList!!.isNotEmpty()) {
                    activity?.apply {
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.DEVICESECURITY_VIEW_LIST,
                            hashMapOf(
                                Pair(
                                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceViewList
                                )
                            ), this
                        )
                    }

                    Navigation.findNavController(view).navigate(
                        R.id.action_myPreferencesFragment_to_viewAllLinkedDevicesFragment,
                        bundleOf(
                            ViewAllLinkedDevicesFragment.DEVICE_LIST to deviceList
                        )
                    )
                }
            }
        }
    }

    private fun deleteAccountShowPopup() {
        view?.findNavController()?.navigate(
            R.id.action_myPreferencesFragment_to_deleteAccountBottomSheetDialog
        )
    }



    @TargetApi(Build.VERSION_CODES.M)
    private fun checkForLocationPermissionAndNavigateToLinkDevice() {
        activity?.apply {
            //Check if user has location services enabled. If not, notify user as per current store locator functionality.
            if (!Utils.isLocationEnabled(this)) {
                val enableLocationSettingsFragment = EnableLocationSettingsFragment()
                enableLocationSettingsFragment?.show(
                    supportFragmentManager,
                    EnableLocationSettingsFragment::class.java.simpleName
                )
                return@apply
            }

            // If location services enabled, extract latitude and longitude
            startLocationDiscoveryProcess()
        }
    }

    private fun startLocationDiscoveryProcess() {
        locator.getCurrentLocation { locationEvent ->
            when (locationEvent) {
                is Event.Location -> handleLocationEvent(locationEvent)
                is Event.Permission -> handlePermissionEvent(locationEvent)
            }
        }
    }

    private fun handlePermissionEvent(permissionEvent: Event.Permission) {
        if (permissionEvent.event == EventType.LOCATION_PERMISSION_NOT_GRANTED) {
            Utils.saveLastLocation(null, activity)
            handleLocationEvent(null)
        }
    }

    private fun handleLocationEvent(locationEvent: Event.Location?) {
        Utils.saveLastLocation(locationEvent?.locationData, context)
        navigateToLinkDeviceFragment()
    }

    private fun navigateToLinkDeviceFragment() {
        activity?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.DEVICESECURITY_LINK_START,
                hashMapOf(
                    Pair(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE,
                        FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceInitiated
                    )
                ), this
            )
        }
        view?.let {
            try {
                Navigation.findNavController(it)
                    .navigate(R.id.action_myPreferencesFragment_to_navigation)
            } catch (e: Exception) {
                FirebaseManager.logException(e)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            LOCK_REQUEST_CODE_TO_ENABLE -> {
                setUserAuthentication(resultCode == Activity.RESULT_OK)
                if (resultCode == Activity.RESULT_OK) {
                    AuthenticateUtils.enableBiometricForCurrentSession(false)
                }
            }
            LOCK_REQUEST_CODE_TO_DISABLE -> setUserAuthentication(resultCode != Activity.RESULT_OK)
            SECURITY_SETTING_REQUEST_CODE -> if (AuthenticateUtils.isDeviceSecure(requireContext())) {
                startBiometricAuthentication(LOCK_REQUEST_CODE_TO_ENABLE)
            } else {
                setUserAuthentication(false)
            }
            REQUEST_SUBURB_CHANGE -> {
                val lastDeliveryLocation = Utils.getPreferredDeliveryLocation()
                lastDeliveryLocation?.let { setDeliveryLocation(it) }
            }
            SECURITY_SETTING_REQUEST_DIALOG -> if (resultCode == Activity.RESULT_OK) {
                try {
                    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE)
                } catch (ex: Exception) {
                    setUserAuthentication(false)
                }
            } else {
                setUserAuthentication(false)
            }
            SECURITY_INFO_REQUEST_DIALOG -> startBiometricAuthentication(
                LOCK_REQUEST_CODE_TO_DISABLE
            )
            else -> {
            }
        }
    }

    fun startBiometricAuthentication(requestCode: Int) {
        try {
            AuthenticateUtils.startAuthenticateApp(requireActivity(), requestCode)
        } catch (e: Exception) {
           FirebaseManager.logException(e)
        }
    }

    fun setUserAuthentication(isAuthenticated: Boolean) {
        AuthenticateUtils
            .setUserAuthenticate(if (isAuthenticated) SessionDao.BIOMETRIC_AUTHENTICATION_STATE.ON else SessionDao.BIOMETRIC_AUTHENTICATION_STATE.OFF)
        binding.biometricAuthenticationSwitchCompat?.isChecked = isAuthenticated
    }

    fun openDeviceSecuritySettings() {
        Utils.displayValidationMessageForResult(
            activity,
            CustomPopUpWindow.MODAL_LAYOUT.SET_UP_BIOMETRICS_ON_DEVICE,
            "",
            SECURITY_SETTING_REQUEST_DIALOG
        )
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.PREFERENCES)
        bindDataWithUI()
    }

    private fun locationSelectionClicked() {
        activity?.apply {
            if (this is MyPreferencesInterface) {
                hideToolbar()
            }
        }

        if (SessionUtilities.getInstance().isUserAuthenticated) {
            activity?.apply {
                KotlinUtils.presentEditDeliveryGeoLocationActivity(
                    this,
                    REQUEST_CODE,
                    KotlinUtils.getPreferredDeliveryType(),
                    Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                )
            }
        } else {
            ScreenManager.presentSSOSignin(activity,
                StandardDeliveryFragment.DEPARTMENT_LOGIN_REQUEST)
        }

    }

    fun setDeliveryLocation(shoppingDeliveryLocation: ShoppingDeliveryLocation?) {
        binding.apply {
            iconCaretEnd.visibility = View.GONE
            editLocation.visibility = View.VISIBLE
            deliverLocationIcon.setBackgroundResource(R.drawable.tick_cli_active)
            shoppingDeliveryLocation?.let {
                setDeliveryAddressView(
                    activity,
                    shoppingDeliveryLocation.fulfillmentDetails,
                    tvDeliveringTo,
                    tvDeliveryLocation,
                    null,
                    true
                )
            }
        }
    }

    companion object {

        const val RESULT_LISTENER_DELETE_DEVICE: String = "deleteDevice"
        const val RESULT_LISTENER_LINK_DEVICE = "linkDevice"
        const val IS_DEVICE_LINKED = "isLinked"
        const val LOCK_REQUEST_CODE_TO_ENABLE = 222
        const val LOCK_REQUEST_CODE_TO_DISABLE = 333
        const val SECURITY_SETTING_REQUEST_CODE = 232
        const val SECURITY_SETTING_REQUEST_DIALOG = 234
        const val SECURITY_INFO_REQUEST_DIALOG = 235
        const val REQUEST_SUBURB_CHANGE = 143
        const val IS_NON_WFS_USER = "isNonWFSUser"
        const val DEVICE_LIST = "deviceList"
    }

    override fun onDestroy() {
        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN) }
        super.onDestroy()
    }

    override fun tryAgain() {
        callDeleteAccountApi()
    }
    private fun showErrorDialog() {
        requireActivity().resources?.apply {
            vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                this@MyPreferencesFragment,
                requireActivity(),
                getString(R.string.vto_generic_error),
                "",
                getString(R.string.retry_label)
            )
        }
    }

    private fun showHuaweiToken() {
        //need to un comment before merging the code
        // if (BuildConfig.DEBUG && BuildConfig.FLAVOR == AppConstant.HUAWEI_FLAVOR) {
            binding.apply {
                huaweiTokenLayout.visibility = View.VISIBLE
                huaweiTokenTv.text = WoolworthsApplication.getInstance().userManager.huaweiPushToken
            }
       // }
    }
}
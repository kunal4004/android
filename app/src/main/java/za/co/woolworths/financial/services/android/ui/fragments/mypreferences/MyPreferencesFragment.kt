package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_my_preferences.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment.RESULT_CODE_DEVICE_LINKED
import za.co.woolworths.financial.services.android.ui.fragments.mypreferences.ViewAllLinkedDevicesFragment.Companion.DEVICE_LIST
import za.co.woolworths.financial.services.android.util.AuthenticateUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.presentEditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.setDeliveryAddressView
import za.co.woolworths.financial.services.android.util.Utils


class MyPreferencesFragment : Fragment(), View.OnClickListener, View.OnTouchListener {

    private var isNonWFSUser: Boolean = true
    private var mViewAllLinkedDevices: Call<ViewAllLinkedDeviceResponse>? = null
    private var deviceList: ArrayList<UserDevice>? = ArrayList(0)
    private var isUpdateAccountCache: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            isNonWFSUser = getBoolean(IS_NON_WFS_USER)
            val list = getSerializable(DEVICE_LIST)
            if (list is ArrayList<*> && list.isNotEmpty()) {
                deviceList = list as ArrayList<UserDevice>
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
        activity?.runOnUiThread { activity?.window?.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN) }

        setFragmentResultListener(RESULT_LISTENER_LINK_DEVICE) { requestKey, bundle ->
            Utils.setLinkConfirmationShown(true)
            isUpdateAccountCache = true
            callLinkedDevicesAPI()
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        bindDataWithUI()
    }


    private fun init() {
        auSwitch.setOnClickListener(this)
        locationSelectedLayout.setOnClickListener(this)
        auSwitch.setOnTouchListener(this)
        linkDeviceSwitch.setOnClickListener(this)
        retryLinkDeviceLinearLayout?.setOnClickListener(this)
        viewAllLinkedDevicesRelativeLayout?.setOnClickListener(this)

        activity?.apply {
            if (this is MyPreferencesInterface) {
                setToolbarTitle(getString(R.string.acc_my_preferences))
                setToolbarTitleGravity(Gravity.START)
            }
        }
    }

    fun bindDataWithUI() {
        if (AuthenticateUtils.getInstance(activity).isAppSupportsAuthentication) {
            if (AuthenticateUtils.getInstance(activity).isDeviceSecure) auSwitch.isChecked = AuthenticateUtils.getInstance(activity).isAuthenticationEnabled else setUserAuthentication(false)
        } else {
            biometricsLayout.setVerticalGravity(View.GONE)
        }
        val lastDeliveryLocation = Utils.getPreferredDeliveryLocation()
        lastDeliveryLocation?.let { setDeliveryLocation(it) }

        val isDeviceIdentityIdPresent = verifyDeviceIdentityId(deviceList)
        updateLinkedDeviceView(isDeviceIdentityIdPresent)
    }

    private fun callLinkedDevicesAPI() {
        val spinningAnimation = KotlinUtils.rotateViewAnimation()
        retryLinkDeviceImageView?.startAnimation(spinningAnimation)
        retryLinkDeviceLinearLayout?.visibility = View.VISIBLE
        retryLinkDeviceTextView?.visibility = View.GONE

        mViewAllLinkedDevices = OneAppService.getAllLinkedDevices(isUpdateAccountCache)
        mViewAllLinkedDevices?.enqueue(CompletionHandler(object : IResponseListener<ViewAllLinkedDeviceResponse> {

            override fun onSuccess(response: ViewAllLinkedDeviceResponse?) {
                when (response?.httpCode) {
                    200 -> {
                        if (!isAdded) {
                            return
                        }
                        spinningAnimation.cancel()
                        retryLinkDeviceLinearLayout?.visibility = View.GONE
                        val isDeviceIdentityIdPresent = verifyDeviceIdentityId(response?.userDevices)
                        deviceList = response?.userDevices
                        updateLinkedDeviceView(isDeviceIdentityIdPresent)
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

    private fun showLinkDeviceRetryView() {
        retryLinkDeviceLinearLayout?.visibility = View.VISIBLE
        retryLinkDeviceTextView?.visibility = View.VISIBLE
        linkDeviceSwitch?.visibility = View.GONE
    }

    private fun updateLinkedDeviceView(deviceIdentityIdPresent: Boolean) {
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
                        linkThisDeviceTextView?.text = getString(R.string.link_device_this_is_linked)
                    }
                } else {
                    linkDeviceSwitch.visibility = View.VISIBLE
                    linkDeviceSwitch.isEnabled = true
                    context?.apply {
                        linkThisDeviceTextView?.text = getString(R.string.my_preferences_link_this_device)
                    }
                }
            }
        }
    }

    private fun verifyDeviceIdentityId(userDevices: ArrayList<UserDevice>?): Boolean {
        var isPresent = false
        when {
            userDevices != null && userDevices.isNotEmpty() -> {
                val appInstanceId = Utils.getUniqueDeviceID(context)
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
            R.id.auSwitch -> if (AuthenticateUtils.getInstance(activity).isDeviceSecure) {
                if (auSwitch.isChecked) {
                    startBiometricAuthentication(LOCK_REQUEST_CODE_TO_ENABLE)
                } else {
                    Utils.displayValidationMessageForResult(activity, CustomPopUpWindow.MODAL_LAYOUT.BIOMETRICS_SECURITY_INFO, getString(R.string.biometrics_security_info), SECURITY_INFO_REQUEST_DIALOG)
                }
            } else openDeviceSecuritySettings()
            R.id.locationSelectedLayout -> locationSelectionClicked()
            R.id.linkDeviceSwitch -> {
                if (linkDeviceSwitch!!.isChecked) {
                    activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.DEVICESECURITY_LINK_START, hashMapOf(Pair(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE, FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceInitiated)), this) }
                    Navigation.findNavController(view).navigate(R.id.action_myPreferencesFragment_to_navigation)
                } else {
                    Navigation.findNavController(view).navigate(R.id.action_myPreferencesFragment_to_unlinkDeviceBottomSheetFragment)
                }
            }
            R.id.retryLinkDeviceLinearLayout -> {
                callLinkedDevicesAPI()
            }
            R.id.viewAllLinkedDevicesRelativeLayout -> {
                if (deviceList != null && deviceList!!.isNotEmpty()) {
                    activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.DEVICESECURITY_VIEW_LIST, hashMapOf(Pair(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE, FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceViewList)), this) }

                    Navigation.findNavController(view).navigate(R.id.action_myPreferencesFragment_to_viewAllLinkedDevicesFragment,
                            bundleOf(
                                    ViewAllLinkedDevicesFragment.DEVICE_LIST to deviceList
                            ))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            LOCK_REQUEST_CODE_TO_ENABLE -> {
                setUserAuthentication(resultCode == Activity.RESULT_OK)
                if (resultCode == Activity.RESULT_OK) {
                    AuthenticateUtils.getInstance(activity).enableBiometricForCurrentSession(false)
                }
            }
            LOCK_REQUEST_CODE_TO_DISABLE -> setUserAuthentication(resultCode != Activity.RESULT_OK)
            SECURITY_SETTING_REQUEST_CODE -> if (AuthenticateUtils.getInstance(activity).isDeviceSecure) {
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
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE)
                } catch (ex: Exception) {
                    setUserAuthentication(false)
                }
            } else {
                setUserAuthentication(false)
            }
            SECURITY_INFO_REQUEST_DIALOG -> startBiometricAuthentication(LOCK_REQUEST_CODE_TO_DISABLE)
            else -> {
            }
        }
    }

    fun startBiometricAuthentication(requestCode: Int) {
        try {
            AuthenticateUtils.getInstance(activity).startAuthenticateApp(requestCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUserAuthentication(isAuthenticated: Boolean) {
        AuthenticateUtils.getInstance(activity).setUserAuthenticate(if (isAuthenticated) SessionDao.BIOMETRIC_AUTHENTICATION_STATE.ON else SessionDao.BIOMETRIC_AUTHENTICATION_STATE.OFF)
        auSwitch.isChecked = isAuthenticated
    }

    fun openDeviceSecuritySettings() {
        Utils.displayValidationMessageForResult(activity, CustomPopUpWindow.MODAL_LAYOUT.SET_UP_BIOMETRICS_ON_DEVICE, "", SECURITY_SETTING_REQUEST_DIALOG)
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.PREFERENCES)
        bindDataWithUI()
    }

    private fun locationSelectionClicked() {
        presentEditDeliveryLocationActivity(activity, REQUEST_SUBURB_CHANGE, null)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (view.id) {
            R.id.auSwitch -> return motionEvent.actionMasked == MotionEvent.ACTION_MOVE
            else -> {
            }
        }
        return false
    }

    fun setDeliveryLocation(shoppingDeliveryLocation: ShoppingDeliveryLocation?) {
        iconCaretRight.visibility = View.GONE
        editLocation.visibility = View.VISIBLE
        deliverLocationIcon.setBackgroundResource(R.drawable.tick_cli_active)
        shoppingDeliveryLocation?.let {
            setDeliveryAddressView(activity, shoppingDeliveryLocation, tvDeliveringTo, tvDeliveryLocation, null)
        }
    }

    companion object {

        const val RESULT_LISTENER_DELETE_DEVICE: String = "deleteDevice"
        const val RESULT_LISTENER_LINK_DEVICE = "linkDevice"
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
}
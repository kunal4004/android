package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_my_preferences.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.util.AuthenticateUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.presentEditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.setDeliveryAddressView
import za.co.woolworths.financial.services.android.util.Utils


class MyPreferencesFragment : Fragment(), View.OnClickListener, View.OnTouchListener {

    private var isNonWFSUser: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            isNonWFSUser = getBoolean(IS_NON_WFS_USER)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setFragmentResultListener("linkDevice") { requestKey, bundle ->
            Utils.setLinkDeviceConfirmationShown(true)
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
        activity?.apply {
            if (this is MyPreferencesInterface) {
                setToolbarTitle(getString(R.string.acc_my_preferences))
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

        when (isNonWFSUser) {
            true -> {
                linkDeviceLayout?.visibility = View.GONE
            }
            else -> {
                linkDeviceSwitch.isChecked = !TextUtils.isEmpty(Utils.getLinkedDeviceToken())
                if (!TextUtils.isEmpty(Utils.getLinkedDeviceToken())) {
                    linkDeviceSwitch.visibility = View.GONE
                    linkDeviceSwitch.isEnabled = TextUtils.isEmpty(Utils.getLinkedDeviceToken())
                    context?.apply {
                        linkThisDeviceTextView?.text = getString(R.string.link_device_this_is_linked)
                    }
                }
            }
        }
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
            R.id.linkDeviceSwitch -> if (linkDeviceSwitch!!.isChecked) {
                Log.e(TAG, "checked")
                Navigation.findNavController(view).navigate(R.id.action_myPreferencesFragment_to_navigation)
            } else {
                Log.e(TAG, "unchecked")
                Navigation.findNavController(view).navigate(R.id.action_myPreferencesFragment_to_unlinkDeviceBottomSheetFragment)
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
        @JvmStatic
        fun newInstance() =
                MyPreferencesFragment().apply {
                    arguments = Bundle().apply {
                    }
                }

        private const val TAG = "MyPreferencesFragment"
        const val LOCK_REQUEST_CODE_TO_ENABLE = 222
        const val LOCK_REQUEST_CODE_TO_DISABLE = 333
        const val SECURITY_SETTING_REQUEST_CODE = 232
        const val SECURITY_SETTING_REQUEST_DIALOG = 234
        const val SECURITY_INFO_REQUEST_DIALOG = 235
        const val REQUEST_SUBURB_CHANGE = 143
        const val IS_NON_WFS_USER = "isNonWFSUser"
    }
}
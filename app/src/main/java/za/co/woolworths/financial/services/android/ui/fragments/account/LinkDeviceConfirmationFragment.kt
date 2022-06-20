package za.co.woolworths.financial.services.android.ui.fragments.account

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.current_location_row_layout.*
import kotlinx.android.synthetic.main.fragment_link_device_from_account_prod.*
import kotlinx.android.synthetic.main.layout_link_device_result.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationInterface
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.personal_loan.PersonalLoanFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.StoreCardOptionsFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardDetailFragment
import za.co.woolworths.financial.services.android.ui.fragments.statement.StatementFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import java.util.*


class LinkDeviceConfirmationFragment : Fragment(), View.OnClickListener {

    private lateinit var locator: Locator
    private var mApplyNowState: ApplyNowState? = null
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mApplyNowState = it.getSerializable(AccountSignedInPresenterImpl.APPLY_NOW_STATE) as? ApplyNowState
                    ?: ApplyNowState.STORE_CARD
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
        activity?.runOnUiThread { activity?.window?.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN) }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_link_device_from_account_prod, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locator = Locator(activity as AppCompatActivity)
        activity?.let {
            if (it is LinkDeviceConfirmationInterface) {
                toolbar = it.getToolbar() as Toolbar
            }
        }
        Utils.setLinkConfirmationShown(true)
        val skipButton: TextView = toolbar?.findViewById(R.id.linkDeviceConfirmToolbarRightButton) as TextView
        skipButton.setOnClickListener(this)

        linkDeviceConfirmationButton.setOnClickListener {
            checkForLocationPermissionAndNavigateToLinkDevice()
        }

        context?.let {
            val deviceSecurity = AppConfigSingleton.deviceSecurity
            when(mApplyNowState){
                ApplyNowState.STORE_CARD ->{
                    linkDeviceConfirmationHeaderIcon?.setImageResource(R.drawable.sc_asset)
                    linkDeviceConfirmationTitle?.text = deviceSecurity?.storeCard?.primaryDeviceConfirmation?.title
                    linkDeviceConfirmationDesc?.text = deviceSecurity?.storeCard?.primaryDeviceConfirmation?.description
                }
                ApplyNowState.PERSONAL_LOAN ->{
                    linkDeviceConfirmationHeaderIcon?.setImageResource(R.drawable.pl_asset)
                    linkDeviceConfirmationTitle?.text = deviceSecurity?.personalLoan?.primaryDeviceConfirmation?.title
                    linkDeviceConfirmationDesc?.text = deviceSecurity?.personalLoan?.primaryDeviceConfirmation?.description
                }
                ApplyNowState.SILVER_CREDIT_CARD,
                ApplyNowState.GOLD_CREDIT_CARD,
                ApplyNowState.BLACK_CREDIT_CARD ->
                {
                    linkDeviceConfirmationHeaderIcon?.setImageResource(R.drawable.cc_asset)
                    linkDeviceConfirmationTitle?.text = deviceSecurity?.creditCard?.primaryDeviceConfirmation?.title
                    linkDeviceConfirmationDesc?.text = deviceSecurity?.creditCard?.primaryDeviceConfirmation?.description
                }
            }
        }
    }

    private fun navigateToLinkDeviceFragment() {
        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.DEVICESECURITY_LINK_START,
            hashMapOf(Pair(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE, FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceInitiated)), this) }
        val navController = view?.let { Navigation.findNavController(it) }
        navController?.navigate(R.id.action_linkDeviceConfirmationFragment_to_otp_navigation, bundleOf(
            AccountSignedInPresenterImpl.APPLY_NOW_STATE to mApplyNowState
        ))
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            if (it is LinkDeviceConfirmationInterface) {
                it.showToolbarButton()
                it.hideBackButton()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.linkDeviceConfirmToolbarRightButton -> {
                onSkipPressed()
            }
        }
    }

    private fun onSkipPressed() {
        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.DEVICESECURITY_LINK_SKIP, hashMapOf(Pair(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE, FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceSkipped)), this) }

        context?.let {
            linkDeviceResultIcon?.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_skip))

            linkDeviceResultTitle?.text = it.getString(R.string.device_not_linked)
            linkDeviceResultSubitle?.text = it.getString(R.string.not_linked_device_desc)

            gotItLinkDeviceConfirmationButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    clearAllFlags()
                    val intent = Intent()
                    intent.putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, mApplyNowState)
                    activity?.setResult(MyAccountsFragment.RESULT_CODE_LINK_DEVICE, intent)
                    activity?.finish()
                }
            }
            linkMyDeviceConfirmationButton.apply {
                visibility = View.VISIBLE
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                setOnClickListener {
                    checkForLocationPermissionAndNavigateToLinkDevice()
                }
            }
        }
        linkDeviceResultLayout.visibility = View.VISIBLE
        linkDeviceConfirmationScrollLayout.visibility = View.GONE

        activity?.apply {
            if (this is LinkDeviceConfirmationActivity) {
                supportActionBar?.let {
                    it.setDisplayHomeAsUpEnabled(false)
                }
            }
            if (this is LinkDeviceConfirmationInterface) {
                hideToolbarButton()
            }
        }
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

    fun clearAllFlags(){
        MyCardDetailFragment.apply {
            FREEZE_CARD_DETAIL = false
            BLOCK_CARD_DETAIL = false
            PAY_WITH_CARD_DETAIL = false
        }
        StoreCardOptionsFragment.apply {
            GET_REPLACEMENT_CARD_DETAIL = false
            ACTIVATE_VIRTUAL_CARD_DETAIL = false
        }
        PersonalLoanFragment.apply{
            PL_WITHDRAW_FUNDS_DETAIL = false
        }
        StatementFragment.VIEW_STATEMENT_DETAIL = false
        StatementFragment.SEND_STATEMENT_DETAIL = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EnableLocationSettingsFragment.ACCESS_MY_LOCATION_REQUEST_CODE) {
            startLocationDiscoveryProcess()
        }
    }

    override fun onDestroy() {
        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN) }
        super.onDestroy()
    }
}
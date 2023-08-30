package za.co.woolworths.financial.services.android.ui.fragments.account

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentLinkDeviceFromAccountProdBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationInterface
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.personal_loan.PersonalLoanFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.statement.StatementFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_device_security.navigation.DeviceSecurityActivityResult.Companion.RESULT_CODE_LINK_DEVICE
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator

class LinkDeviceConfirmationFragment : BaseFragmentBinding<FragmentLinkDeviceFromAccountProdBinding>(FragmentLinkDeviceFromAccountProdBinding::inflate), View.OnClickListener {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) }
        activity?.runOnUiThread { activity?.window?.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN) }

        locator = Locator(activity as AppCompatActivity)
        activity?.let {
            if (it is LinkDeviceConfirmationInterface) {
                toolbar = it.getToolbar() as Toolbar
            }
        }
        Utils.setLinkConfirmationShown(true)
        val skipButton: TextView = toolbar?.findViewById(R.id.linkDeviceConfirmToolbarRightButton) as TextView
        skipButton.setOnClickListener(this)

        binding.apply {
            linkDeviceConfirmationButton.setOnClickListener {
                checkForLocationPermissionAndNavigateToLinkDevice()
            }

            context?.let {
                val deviceSecurity = AppConfigSingleton.deviceSecurity
                when (mApplyNowState) {
                    ApplyNowState.STORE_CARD -> {
                        linkDeviceConfirmationHeaderIcon?.setImageResource(R.drawable.sc_asset)
                        linkDeviceConfirmationTitle?.text =
                            deviceSecurity?.storeCard?.primaryDeviceConfirmation?.title
                        linkDeviceConfirmationDesc?.text =
                            deviceSecurity?.storeCard?.primaryDeviceConfirmation?.description
                    }
                    ApplyNowState.PERSONAL_LOAN -> {
                        linkDeviceConfirmationHeaderIcon?.setImageResource(R.drawable.pl_asset)
                        linkDeviceConfirmationTitle?.text =
                            deviceSecurity?.personalLoan?.primaryDeviceConfirmation?.title
                        linkDeviceConfirmationDesc?.text =
                            deviceSecurity?.personalLoan?.primaryDeviceConfirmation?.description
                    }
                    ApplyNowState.SILVER_CREDIT_CARD,
                    ApplyNowState.GOLD_CREDIT_CARD,
                    ApplyNowState.BLACK_CREDIT_CARD -> {
                        linkDeviceConfirmationHeaderIcon?.setImageResource(R.drawable.cc_asset)
                        linkDeviceConfirmationTitle?.text =
                            deviceSecurity?.creditCard?.primaryDeviceConfirmation?.title
                        linkDeviceConfirmationDesc?.text =
                            deviceSecurity?.creditCard?.primaryDeviceConfirmation?.description
                    }
                    else -> {}
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
                binding.onSkipPressed()
            }
        }
    }

    private fun FragmentLinkDeviceFromAccountProdBinding.onSkipPressed() {
        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.DEVICESECURITY_LINK_SKIP, hashMapOf(Pair(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE, FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceSkipped)), this) }

        linkDeviceResultLayout.apply {
            context?.let {
                linkDeviceResultIcon?.setImageDrawable(
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.ic_skip
                    )
                )

                linkDeviceResultTitle?.text = it.getString(R.string.device_not_linked)
                linkDeviceResultSubitle?.text = it.getString(R.string.not_linked_device_desc)

                gotItLinkDeviceConfirmationButton.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        val intent = Intent()
                        intent.putExtra(
                            AccountSignedInPresenterImpl.APPLY_NOW_STATE,
                            mApplyNowState
                        )
                        if (StoreCardActivity.FREEZE_CARD_DETAIL) {
                            activity?.setResult(Activity.RESULT_CANCELED, intent)
                        } else {
                            activity?.setResult(RESULT_CODE_LINK_DEVICE, intent)
                        }
                        clearAllFlags()
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
        }
        linkDeviceResultLayout.root.visibility = View.VISIBLE
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

    private fun clearAllFlags(){
        StoreCardActivity.apply {
            SHOW_TEMPORARY_FREEZE_DIALOG = false
            FREEZE_CARD_DETAIL = false
            SHOW_BLOCK_CARD_SCREEN = false
            BLOCK_CARD_DETAIL = false
            SHOW_PAY_WITH_CARD_SCREEN = false
            PAY_WITH_CARD_DETAIL = false
            SHOW_GET_REPLACEMENT_CARD_SCREEN = false
            GET_REPLACEMENT_CARD_DETAIL = false
            SHOW_ACTIVATE_VIRTUAL_CARD_SCREEN = false
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
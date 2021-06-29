package za.co.woolworths.financial.services.android.ui.fragments.account

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.fragment_link_device_from_account_prod.*
import kotlinx.android.synthetic.main.layout_link_device_result.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationInterface
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.FuseLocationAPISingleton
import za.co.woolworths.financial.services.android.util.Utils


class LinkDeviceConfirmationFragment : Fragment(), View.OnClickListener {

    private var mApplyNowState: ApplyNowState? = null
    private var toolbar: Toolbar? = null
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher. You can use either a val, as shown in this snippet,
    // or a lateinit var in your onAttach() or onCreate() method.
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                askToEnableLocationSettings()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                navigateToLinkDeviceFragment()
            }
        }

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
        activity?.let {
            if (it is LinkDeviceConfirmationInterface) {
                toolbar = it.getToolbar() as Toolbar
            }
        }
        Utils.setLinkConfirmationShown(true)
        val skipButton: TextView = toolbar?.findViewById(R.id.linkDeviceConfirmToolbarRightButton) as TextView
        skipButton.setOnClickListener(this)

        linkDeviceConfirmationButton.setOnClickListener {
            askLocationPermission()
        }

    }

    private fun askLocationPermission() {
        context?.let { context ->
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    askToEnableLocationSettings()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected. In this UI,
                    // include a "cancel" or "no thanks" button that allows the user to
                    // continue using your app without granting the permission.
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
            }
        }
    }

    private fun askToEnableLocationSettings() {
        activity?.apply {
            val locationRequest = LocationRequest.create()?.apply {
                interval = 100
                fastestInterval = 500
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val client: SettingsClient = LocationServices.getSettingsClient(this)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
            task.addOnSuccessListener { locationSettingsResponse ->
                // All location settings are satisfied. The client can initialize
                // location requests here.
                navigateToLinkDeviceFragment()
            }

            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(
                            this,
                            FuseLocationAPISingleton.REQUEST_CHECK_SETTINGS
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
                //Even if fails to enable location settings navigate to link device
                navigateToLinkDeviceFragment()
            }

        }

    }

    private fun navigateToLinkDeviceFragment() {
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.DEVICESECURITY_LINK_START,
            hashMapOf(Pair(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE, FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceInitiated)))
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
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.DEVICESECURITY_LINK_SKIP, hashMapOf(Pair(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE, FirebaseManagerAnalyticsProperties.PropertyNames.linkDeviceSkipped)))

        context?.let {
            linkDeviceResultIcon?.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_skip))
            linkDeviceResultTitle?.text = it.getString(R.string.ok_cool)
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
            Handler().postDelayed({

                val intent = Intent()
                intent.putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, mApplyNowState)
                setResult(MyAccountsFragment.RESULT_CODE_LINK_DEVICE, intent)
                finish()
            }, AppConstant.DELAY_1500_MS)
        }
    }

    override fun onDestroy() {
        activity?.runOnUiThread { activity?.window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN) }
        super.onDestroy()
    }
}
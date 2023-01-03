package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProcessRequestFragmentBinding
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import java.net.ConnectException

class PMAProcessRequestFragment : ProcessYourRequestFragment(), View.OnClickListener {

    private var menuItem: MenuItem? = null
    private var navController: NavController? = null
    private var callUsNumber: String? = "0861 50 20 20"

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        navController = Navigation.findNavController(view)

        circularProgressListener({}, {}) // onSuccess(), onFailure()

        binding.processRequestNavHostFragment.includePMAProcessingFailure.apply {
            btnRetryProcessPayment?.apply {
                setOnClickListener(this@PMAProcessRequestFragment)
                AnimationUtilExtension.animateViewPushDown(this)
            }

            callCenterNumberTextView?.apply {
                setOnClickListener(this@PMAProcessRequestFragment)
                AnimationUtilExtension.animateViewPushDown(this)
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }
        }

        autoConnection()
    }

    private fun ProcessRequestFragmentBinding.updateUIOnFailure(desc: String?) {
        menuItem?.isVisible = true
        desc?.apply {
            // keep numeric characters only
            val number = this.replace("[^0-9]".toRegex(), "")
            if (number.isNotEmpty()) {
                processRequestNavHostFragment.includePMAProcessingFailure.callCenterNumberTextView?.visibility = VISIBLE
                callUsNumber = number
            } else {
                callUsNumber = "0861 50 20 20"
                processRequestNavHostFragment.includePMAProcessingFailure.callCenterNumberTextView?.visibility = GONE
            }
        }
        stopSpinning(false)
        processRequestNavHostFragment.apply {
            includePMAProcessingFailure.processResultFailureTextView?.text = desc
            includePMAProcessingSuccess?.root?.visibility = GONE
            includePMAProcessingFailure?.root?.visibility = VISIBLE
            includePMAProcessing?.root?.visibility = GONE
        }
    }

    private fun ProcessRequestFragmentBinding.showPMAProcess() {
        menuItem?.isVisible = false
        processRequestNavHostFragment.apply {
            includePMAProcessingSuccess?.root?.visibility = GONE
            includePMAProcessingFailure?.root?.visibility = GONE
            includePMAProcessing?.root?.visibility = VISIBLE
            includePMAProcessing?.processRequestTitleTextView?.text = bindString(R.string.processing_your_request)
        }
    }

    private fun autoConnection() {
        activity?.let { activity ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(activity, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    when (hasConnection && !payMyAccountViewModel.isRedirectionAPICompleted()) {
                        true -> queryServicePostPayU()
                        else -> return
                    }
                }
            })
        }
    }

    private fun setupToolbar() {
        (activity as? PayMyAccountActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            binding.showPMAProcess()
            configureToolbar("")
            displayToolbarDivider(false)
        }
    }

    private fun queryServicePostPayU() {
        startSpinning()
        payMyAccountViewModel.queryServicePostPayU({
            // check to prevent action_PMAProcessRequestFragment_to_secure3DPMAFragment throws IllegalStateException
            // TODO:: Find a way to handle path does not exist in navigation graph
            if ((activity as? PayMyAccountActivity)?.currentFragment !is PMAProcessRequestFragment) return@queryServicePostPayU
            //Success
            fragmentIsVisible()
            stopSpinning(true)
            val options = NavOptions.Builder().setPopUpTo(R.id.pmaProcessRequestFragment, true).build()
            navController?.navigate(R.id.action_PMAProcessRequestFragment_to_secure3DPMAFragment, null, options)
        }, { stsParams ->
            //Session expired
            fragmentIsVisible()
            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, stsParams, activity)

        }, { errorDesc ->
            //Display popup message
            fragmentIsVisible()
            binding.updateUIOnFailure(errorDesc)

        }, { error ->
            fragmentIsVisible()
            activity?.apply {
                stopSpinning(false)
                if (error is ConnectException) {
                    ErrorHandlerView(this).showToast()
                }
            }
        })
    }

    private fun fragmentIsVisible() {
        if (!isAdded) return
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnRetryProcessPayment -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    binding.showPMAProcess()
                    queryServicePostPayU()
                } else {
                    ErrorHandlerView(activity).showToast()
                }
            }
            R.id.callCenterNumberTextView -> {
                Utils.makeCall(callUsNumber)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.close_menu_item, menu)
        menuItem = menu.findItem(R.id.closeIcon)
        menuItem?.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.closeIcon -> {
                activity?.finish()
                activity?.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse
import za.co.woolworths.financial.services.android.ui.activities.WRewardsVoucherDetailsActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsVoucherListAdapter
import za.co.woolworths.financial.services.android.ui.views.ScrollingLinearLayoutManager
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.AuthenticateUtils
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.BiometricCallback
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.WfsBiometricManager
import za.co.woolworths.financial.services.android.ui.wfs.common.blur.BlurView
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@AndroidEntryPoint
class WRewardsVouchersFragment : Fragment(R.layout.wrewards_vouchers_fragment) {

    private var recyclerView: RecyclerView? = null
    private var voucherResponse: VoucherResponse? = null
    private var mVoucherFrameLayoutContainer : FrameLayout? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    var selectedVoucherPosition = 0
    private var relEmptyStateHandler: RelativeLayout? = null
    private var isBiometricAuthenticated : Boolean = false
    @Inject lateinit var biometricManager : WfsBiometricManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        voucherResponse = Gson().fromJson(arguments?.getString(WREWARDS), VoucherResponse::class.java)
        initView(view)
        initComposeBlur(view)
        initVouchers()
        setUpBiometricAuthentication()
        uniqueIdsForRewardVoucherAutomation()
    }

    private fun initComposeBlur(view: View) {
        view.findViewById<ComposeView>(R.id.voucherComposeView).setContent {
            OneAppTheme {
                BlurView {
                    Surface(modifier = Modifier.fillMaxSize()) {}
                }
            }
        }
    }

    private fun setUpBiometricAuthentication() {
        val bottomNavigationActivity =  (requireActivity() as? BottomNavigationActivity)
        biometricManager.setupBiometricInWRewardsVouchersOnItemTap(fragment = this,
            bottomNavigation = bottomNavigationActivity?.bottomNavigationById) { callback ->
            when (callback) {
                BiometricCallback.ErrorUserCanceled -> {
                    bottomNavigationActivity?.bottomNavigationById?.currentItem = BottomNavigationActivity.INDEX_TODAY
                    AuthenticateUtils.enableBiometricForCurrentSession(true)
                }
                BiometricCallback.Succeeded -> {
                      if (!isBiometricAuthenticated) {
                          presentVoucherDetail()
                      }
                }
                else -> Unit
            }
        }
    }

    private fun presentVoucherDetail() {
        AuthenticateUtils.enableBiometricForCurrentSession(false)
        startVoucherDetailsActivity()
        isBiometricAuthenticated = true
    }

    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        mVoucherFrameLayoutContainer = view.findViewById<FrameLayout>(R.id.voucherFrameLayout)
        relEmptyStateHandler = view.findViewById(R.id.relEmptyStateHandler)
        mErrorHandlerView = ErrorHandlerView(
            requireActivity(),
            relEmptyStateHandler,
            view.findViewById(R.id.imgEmpyStateIcon),
            view.findViewById(R.id.txtEmptyStateTitle),
            view.findViewById(R.id.txtEmptyStateDesc)
        )
        val mLayoutManager = ScrollingLinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.VERTICAL,
            false, 1500
        )
        recyclerView?.layoutManager = mLayoutManager
    }

    private fun initVouchers() {
        val vouchers = voucherResponse?.voucherCollection?.vouchers
        if (vouchers.isNullOrEmpty() || vouchers.size == 0) {
            presentEmptyVoucherView()
        } else {
            presentVouchersListView(this, voucherResponse)
        }
    }

    private fun uniqueIdsForRewardVoucherAutomation() {
        if (requireActivity().resources != null) {
            recyclerView?.contentDescription = getString(R.string.vouchersLayout)
            relEmptyStateHandler?.contentDescription = getString(R.string.voucher_empty_state)
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(requireActivity(),
            FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_VOUCHERS
        )
    }

    private fun presentEmptyVoucherView() {
        mErrorHandlerView?.apply {
            showEmptyState(0)
            hideIcon()
            hideTitle()
            textDescription(getString(R.string.no_vouchers))
        }
        mVoucherFrameLayoutContainer?.visibility = View.GONE
    }

    private fun presentVouchersListView(fragment: Fragment, vResponse: VoucherResponse?) {
        mErrorHandlerView?.hideEmpyState()
        val voucherListAdapter = WRewardsVoucherListAdapter()
        voucherListAdapter.setItem(vResponse?.voucherCollection?.vouchers ?: listOf())
        mVoucherFrameLayoutContainer?.visibility = View.VISIBLE
        recyclerView?.apply {
        adapter = voucherListAdapter
        addOnItemTouchListener(
            RecycleViewClickListner(requireActivity(), this, object : RecycleViewClickListner.ClickListener {
                    override fun onClick(view: View, position: Int) {
                        selectedVoucherPosition = position
                        if (biometricManager.isBiometricEnabled(fragment.requireContext()) && !isBiometricAuthenticated) {
                            biometricManager.show()
                        }else {
                            presentVoucherDetail()
                        }
                    }
                    override fun onLongClick(view: View, position: Int) {}
                })
        )
        }

    }

    private fun startVoucherDetailsActivity() {
        val intent = Intent(requireActivity(), WRewardsVoucherDetailsActivity::class.java)
        intent.putExtra(VOUCHERS, Utils.objectToJson(voucherResponse?.voucherCollection))
        intent.putExtra(POSITION, selectedVoucherPosition)
        startActivity(intent)
    }

    fun scrollToTop() {
        recyclerView?.smoothScrollToPosition(0)
    }

    companion object {
        const val LOCK_REQUEST_CODE_WREWARDS = 111
        const val VOUCHERS = "VOUCHERS"
        const val POSITION = "POSITION"
        const val WREWARDS = "WREWARDS"
    }

}
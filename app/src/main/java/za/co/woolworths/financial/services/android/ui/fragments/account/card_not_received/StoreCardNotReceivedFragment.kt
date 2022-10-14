package za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.StoreCardVtscCardNotReceivedFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.toolbar.AccountProductsToolbarHelper
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.base.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.SystemBarCompat
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@AndroidEntryPoint
class StoreCardNotReceivedFragment :
    Fragment(R.layout.store_card_vtsc_card_not_received_fragment) {

    private var mToolbarHelper: AccountProductsToolbarHelper? = null

    val viewModel: MyAccountsRemoteApiViewModel by viewModels()

    @Inject lateinit var router: ProductLandingRouterImpl

    @Inject lateinit var statusBarCompat: SystemBarCompat

    private lateinit var binding: StoreCardVtscCardNotReceivedFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mToolbarHelper = (activity as? StoreCardActivity)?.getToolbarHelper()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusBarCompat.setDarkStatusAndNavigationBar()
        binding = StoreCardVtscCardNotReceivedFragmentBinding.bind(view)
        setupViews()
        subscribeObserver()
        fragmentResultListener()
    }

    private fun fragmentResultListener() {
        setFragmentResultListener(CardNotArrivedRequestCode) { _, bundle ->
            when (bundle.getString(CardNotArrivedRequestCode)) {
                StoreCardNotReceivedDialogFragment.REQUEST_CODE_TRY_AGAIN ->
                    binding.cardHasNotArrivedTextView.performClick()
            }
        }
    }

    private fun setupViews() {
        mToolbarHelper?.setCardNotReceivedToolbar { dismiss() }
        with(binding) {
            binding.myCardHasArrivedTextView.paintFlags = binding.myCardHasArrivedTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            cardHasNotArrivedTextView.onClick { onCardHasNotArrivedTap() }
            myCardHasArrivedTextView.onClick {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.VTSC_CARD_RECEIVED, requireActivity())
                dismiss() }
        }
    }

    private fun onCardHasNotArrivedTap() {
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.VTSC_CARD_NOT_DELIVERED, requireActivity())
        queryAPIServiceGetCardNotReceived()
    }

    private fun showProgress(isVisible: Boolean) {
        with(binding) {
            notifyCardNotReceivedProgressbar.visibility = if (isVisible) VISIBLE else GONE
            cardHasNotArrivedTextView.apply {
                setTextColor(ContextCompat.getColor(requireContext(), if (isVisible) R.color.black else R.color.white))
                isEnabled = !isVisible
            }
        }
    }

    private fun subscribeObserver() {
        lifecycleScope.launch {
            viewModel.notifyCardNotReceived.collectLatest {
                with(it) {
                    renderNoConnection { router.showNoConnectionToast(requireActivity()) }
                    renderLoading { showProgress(isLoading) }
                    renderSuccess { successNotificationView() }
                    renderHttpFailureFromServer { httpErrorFromServer(this.output.response) }
                    renderFailure { errorMessage() }
                }
            }
        }
    }

    private fun httpErrorFromServer(response: ServerErrorResponse?) {
        router.routeToCardNotArrivedFailure(findNavController(),response)
    }

    private fun errorMessage() {
        router.routeToCardNotArrivedFailure(findNavController(),null)
    }

    private fun successNotificationView() {
        viewModel.setLocalDateTime(SessionDao.KEY.CARD_NOT_RECEIVED_DIALOG_WAS_SHOWN)
        mToolbarHelper?.hideCloseIcon()
        router.routeToConfirmCardNotReceived(findNavController())
    }

    private fun dismiss() {
        viewModel.setLocalDateTime(SessionDao.KEY.CARD_NOT_RECEIVED_DIALOG_WAS_SHOWN)
        (activity as? StoreCardActivity)?.landingNavController()?.popBackStack()
    }

    private fun queryAPIServiceGetCardNotReceived() {
        viewModel.queryServiceCardNotYetReceived()
    }

    override fun onDestroy() {
        viewModel.isStoreCardNotReceivedDialogFragmentVisible = false
        super.onDestroy()
    }

    companion object {
        val CardNotArrivedRequestCode: String = StoreCardNotReceivedFragment::class.java.simpleName
    }
}
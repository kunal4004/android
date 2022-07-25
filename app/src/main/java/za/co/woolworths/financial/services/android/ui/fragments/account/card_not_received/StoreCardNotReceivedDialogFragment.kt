package za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.StoreCardVtscCardNotReceivedPopupDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.base.ViewBindingBottomSheetFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import javax.inject.Inject

@AndroidEntryPoint
class StoreCardNotReceivedDialogFragment : ViewBindingBottomSheetFragment<StoreCardVtscCardNotReceivedPopupDialogBinding>(), View.OnClickListener {
    val viewModel: MyAccountsRemoteApiViewModel by viewModels()

    @Inject lateinit var router: ProductLandingRouterImpl

    companion object {
        fun newInstance() = StoreCardNotReceivedDialogFragment()
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): StoreCardVtscCardNotReceivedPopupDialogBinding {
        return StoreCardVtscCardNotReceivedPopupDialogBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        subscribeObserver()
    }

    private fun setupViews() {
        with(binding) {
            initialLabel()
            AnimationUtilExtension.animateViewPushDown(actionButtonTextView)
            actionButtonTextView.setOnClickListener(this@StoreCardNotReceivedDialogFragment)
        }
    }

    private fun StoreCardVtscCardNotReceivedPopupDialogBinding.initialLabel() {
        headerTextView.contentDescription =
            getString(R.string.title_text_has_your_card_not_arrived_in_the_post)
        descriptionTextView.contentDescription =
            getString(R.string.copy_text_has_your_card_not_arrived_in_the_post)
        actionButtonTextView.contentDescription = getString(R.string.button_my_card_hasnt_arrived)
    }

    private fun showProgress(isVisible: Boolean) {
        with(binding) {
            if (isVisible){
                initialLabel()
            }
            notifyCardNotReceivedProgressbar.visibility = if (isVisible) VISIBLE else GONE
            actionButtonTextView.apply {
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isVisible) R.color.black else R.color.white
                    )
                )
                isEnabled = !isVisible
            }
        }
    }

    private fun subscribeObserver() {
        lifecycleScope.launch {
            viewModel.notifyCardNotReceived.collectLatest {
                with(it){
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
        with(binding) {
            headerTextView.text = getString(R.string.oops_err_title)
            descriptionTextView.text = response?.desc
            actionButtonTextView.text = getString(R.string.try_again)
        }
    }

    private fun errorMessage() {
        with(binding) {
            headerTextView.text = getString(R.string.oops_err_title)
            descriptionTextView.text = getString(R.string.oops_error_message)
            actionButtonTextView.text = getString(R.string.try_again)

            headerTextView.contentDescription = getString(R.string.vtsc_oops_err_title)
            descriptionTextView.contentDescription = getString(R.string.vtsc_oops_error_message)
            actionButtonTextView.contentDescription = getString(R.string.vtsc_try_again)
        }
    }

    private fun successNotificationView() {
        Utils.sessionDaoSave(SessionDao.KEY.CARD_NOT_RECEIVED_DIALOG_WAS_SHOWN, "1")
        with(binding) {
            headerTextView.text = getString(R.string.vtsc_card_not_arrived_notified_title)
            descriptionTextView.text = getString(R.string.vtsc_card_not_arrived_notified_desc)
            actionButtonTextView.text = getString(R.string.got_it)

            headerTextView.contentDescription = getString(R.string.title_text_thanks_for_letting_us_know)
            descriptionTextView.contentDescription = getString(R.string.copy_text_thanks_for_letting_us_know)
            actionButtonTextView.contentDescription = getString(R.string.button_thanks_for_letting_us_know)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.actionButtonTextView -> {
                activity ?: return
                when (binding.actionButtonTextView.text.toString().lowercase()) {
                    getString(R.string.try_again).lowercase()-> { queryAPIServiceGetCardNotReceived() }
                    getString(R.string.vtsc_card_not_arrived_button_caption).lowercase() -> {
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.VTSC_CARD_NOT_DELIVERED,
                            requireActivity()
                        )
                        queryAPIServiceGetCardNotReceived()
                    }
                    getString(R.string.got_it).lowercase() -> {
                        dismiss()
                    }
                }
            }
        }
    }

    private fun queryAPIServiceGetCardNotReceived() {
        viewModel.queryServiceCardNotYetReceived()
    }
}
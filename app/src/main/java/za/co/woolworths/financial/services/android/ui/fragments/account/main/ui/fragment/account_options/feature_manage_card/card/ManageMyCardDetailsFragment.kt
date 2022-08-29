package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ManageCardDetailsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.StoreCardInfo
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.SystemBarCompat
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.StoreCardActivityResultCallback
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.CallBack
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import javax.inject.Inject

@AndroidEntryPoint
class ManageMyCardDetailsFragment : Fragment(R.layout.manage_card_details_fragment) {

    private var mStoreCardMoreDetail: ManageStoreCardMoreDetail? = null
    private var mOnItemClickListener: ManageCardItemListener? = null
    private var mListOfStoreCardOptions: ManageStoreCardLandingList? = null
    val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()
    private val cardFreezeViewModel: TemporaryFreezeCardViewModel by activityViewModels()

    @Inject lateinit var statusBarCompat: SystemBarCompat

    @Inject lateinit var router: ProductLandingRouterImpl

    @Inject lateinit var storeCardActivityResultCallback : StoreCardActivityResultCallback

    private val activityLauncher = BetterActivityResult.registerActivityForResult(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusBarCompat.setDarkStatusAndNavigationBar()
        setToolbar()
        with(ManageCardDetailsFragmentBinding.bind(view)) {
            mStoreCardMoreDetail = ManageStoreCardMoreDetail(requireContext(),incManageCardDetailsInfoLayout)
            mListOfStoreCardOptions = ManageStoreCardLandingList(cardFreezeViewModel, includeListOptions, this@ManageMyCardDetailsFragment)
            setupView()
            setCardViewPagerNavigationGraph()
            setOnClickListener()

            mListOfStoreCardOptions?.apply {
                setupTemporaryFreezeCardGraph()
                setupVirtualTemporaryCardGraph()
            }

        }

        onBackPressed()
    }

    private fun setToolbar() {
        val isMultipleStoreCardEnabled = viewModel.dataSource.isMultipleStoreCardEnabled()
        (activity as? StoreCardActivity)?.apply {
            getToolbarHelper()?.setManageMyCardDetailsToolbar(isMultipleStoreCardEnabled) {
                viewModel.setRefreshRequestStoreCardCards(true)
                landingNavController()?.popBackStack()
            }
        }
    }

    private fun setCardViewPagerNavigationGraph() = setupGraph(
        R.navigation.account_options_manage_card_nav,
        R.id.manageCardFragmentContainerView,
        R.id.manageCardViewPagerFragment
    )

    fun onBackPressed() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    viewModel.setRefreshRequestStoreCardCards(true)
                    router.routeToAccountOptionsProductLanding((activity as? StoreCardActivity)?.landingNavController())
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun ManageCardDetailsFragmentBinding.setOnClickListener() {
        mOnItemClickListener =
            ManageCardItemListener(requireActivity(), router, includeListOptions).apply {
                onClickIntentObserver.observe(viewLifecycleOwner) {
                    when (it) {
                        is CallBack.IntentCallBack -> {
                            it.intent?.let { intent ->
                                launchStoreCard(intent)
                            }
                        }
                        else->Unit
                    }
                }
            }
    }

    private fun launchStoreCard(intent: Intent) {
        activityLauncher.launch(intent, onActivityResult = { result ->
            if (storeCardActivityResultCallback.linkNewCardCallback(result)) {
                viewModel.requestGetStoreCardCards()
            }
        })
        activity?.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
    }

    private fun setupView() {
        mStoreCardMoreDetail?.setCardHolderName(viewModel.cardHolderName)
        mListOfStoreCardOptions?.hideAllRows()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        val position = cardFreezeViewModel.currentPagePosition.value ?: -1
        mStoreCardMoreDetail?.setupView(viewModel.mStoreCardFeatureType)
        showItems(StoreCardInfo(viewModel.mStoreCardFeatureType, position,
            isPopupVisibleInAccountLanding = false,
            isPopupVisibleInCardDetailLanding = false))
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.onViewPagerPageChangeListener.collectLatest { feature ->
                    showItems(feature)
                    mStoreCardMoreDetail?.setupView(feature.feature)
            }
        }
    }

    private fun showItems(feature: StoreCardInfo) {
        mListOfStoreCardOptions?.showListItem(feature) { result ->
            when (result) {
                is ListCallback.CardNotReceived -> {
                    if (!feature.isPopupVisibleInCardDetailLanding) return@showListItem
                    if (result.isCardNotReceived) mListOfStoreCardOptions?.showCardNotReceivedDialog(
                        this@ManageMyCardDetailsFragment,
                        viewModel
                    )
                }
            }
        }
    }

}

package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsManageCardFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.LoaderType
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.StoreCardActivityResultCallback
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.CallBack
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import javax.inject.Inject

@AndroidEntryPoint
class AccountOptionsManageCardFragment : Fragment(R.layout.account_options_manage_card_fragment) {

    companion object {
        val AccountOptionsLandingKey: String by lazy { AccountOptionsManageCardFragment::class.java.simpleName }
        var SHOW_GET_REPLACEMENT_CARD_SCREEN = false
        var GET_REPLACEMENT_CARD_DETAIL = false
        var SHOW_ACTIVATE_VIRTUAL_CARD_SCREEN = false
        var ACTIVATE_VIRTUAL_CARD_DETAIL = false
    }

    @Inject
    lateinit var router: ProductLandingRouterImpl

    @Inject
    lateinit var connectivityLiveData: ConnectivityLiveData

    @Inject
    lateinit var storeCardActivityResultCallback : StoreCardActivityResultCallback

    private lateinit var mOnItemClickListener: ManageCardItemListener
    private lateinit var mHeaderItems: ManageCardLandingHeaderItems
    private lateinit var mItemList: ManageStoreCardLandingList

    val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()
    private val cardFreezeViewModel: TemporaryFreezeCardViewModel by activityViewModels()

    private val activityLauncher = BetterActivityResult.registerActivityForResult(this)

    private val landingController by lazy { (requireActivity() as? StoreCardActivity)?.landingNavController() }
    private lateinit var locator: Locator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.requestGetStoreCardCards()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(AccountOptionsManageCardFragmentBinding.bind(view)) {
            mHeaderItems = ManageCardLandingHeaderItems(viewModel, this, this@AccountOptionsManageCardFragment)
            mItemList = ManageStoreCardLandingList(
                cardFreezeViewModel,
                includeListOptions,
                this@AccountOptionsManageCardFragment
            )
            setOnClickListener()
            setupView()
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.startLocationDiscoveryProcess() {
        locator = Locator(activity as AppCompatActivity)
        locator.getCurrentLocation { locationEvent ->
            when (locationEvent) {
                is Event.Location -> Utils.saveLastLocation(locationEvent.locationData, activity)
                is Event.Permission -> {
                    if (locationEvent.event == EventType.LOCATION_PERMISSION_NOT_GRANTED) {
                        Utils.saveLastLocation(null, activity)
                    }
                }
            }
        }.apply {
            subscribeObservers()
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.setOnClickListener() {
        mOnItemClickListener = ManageCardItemListener(requireActivity(), router, includeListOptions)
        mOnItemClickListener.onClickIntentObserver.observe(viewLifecycleOwner) {
                    when (it) {
                        is CallBack.IntentCallBack -> {
                            it.intent?.let { intent ->
                                launchStoreCard(intent)
                            }
                        }
                        else -> Unit
                    }
                }



        manageCardText.onClick {
            viewModel.emitEventOnCardTap(viewModel.mStoreCardFeatureType)
        }

        cardFreezeViewModel.onUpshellMessageActivateTempCardTap.observe(viewLifecycleOwner){ wasTapped ->
            if (wasTapped){
                mOnItemClickListener.navigateToActivateVirtualTempCard()
                cardFreezeViewModel.onUpshellMessageActivateTempCardTap.value = false
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

    private fun AccountOptionsManageCardFragmentBinding.setupView() {
        mItemList.hideAllRows()
        setupViewPager()
        setCardLabel()
        startLocationDiscoveryProcess()
        mItemList.setupVirtualTemporaryCardGraph()
        setFragResultListener()
    }

    // The fragment will reload when user navigates back to account options,
    // then refreshRequestStoreCardCards can be consumed if output true
    private fun setFragResultListener() {
        with(viewModel) {
            if (refreshApiModel.refreshRequestStoreCardCards) {
                requestGetStoreCardCards()
                setRefreshRequestStoreCardCards(false)
            }
        }
    }


    private fun setCardLabel() {
        mHeaderItems.setCardLabel()
    }

    private fun setupViewPager() {
        setupGraph(
            R.navigation.account_options_manage_card_nav,
            R.id.manageCardFragmentContainerView,
            R.id.manageCardViewPagerFragment
        )
    }

    private fun AccountOptionsManageCardFragmentBinding.subscribeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            connectivityLiveData.observe(viewLifecycleOwner) { isConnectionAvailable ->
                if (isConnectionAvailable && viewModel.retryNetworkRequest.isConnectionAvailableForGetStoreCard()) {
                    lifecycleScope.launch { viewModel.requestGetStoreCardCards() }
                }
            }
        }
            lifecycleScope.launch {
            with(viewModel) {
                storeCardResponseResult.collectLatest { response ->
                    retryNetworkRequest.popStoreCardRequest()
                    locator.stopService()
                    with(response) {
                        renderNoConnection {
                            retryNetworkRequest.putStoreCardRequest()
                            router.showNoConnectionToast(requireActivity())
                        }

                        renderLoading { showProgress(this@subscribeObservers, this) }

                        renderHttpFailureFromServer {
                            router.routeToServerErrorDialog(
                                requireActivity(),
                                output.response
                            )
                        }

                        renderFailure { router.routeToDefaultErrorMessageDialog(requireActivity()) }

                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.onCardTapEvent.collectLatest {
                if (landingController?.currentDestination?.label?.equals(ManageMyCardDetailsFragment::class.java.simpleName) == true) {
                    return@collectLatest
                }
                landingController?.let { controller -> router.routeToManageMyCardDetails(controller) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.onViewPagerPageChangeListener.collect { feature ->
                setCardLabel()
                mHeaderItems.showHeaderItem(feature)
                mItemList.showListItem(feature) { result ->
                    when (result) {
                        is ListCallback.CardNotReceived -> {
                            if (result.isCardNotReceived && feature.isPopupVisibleInCardDetailLanding) mItemList.showCardNotReceivedDialog(
                                this@AccountOptionsManageCardFragment,
                                viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showProgress(
        accountOptionsManageCardFragmentBinding: AccountOptionsManageCardFragmentBinding,
        loading: ViewState.Loading
    ) {
        when (viewModel.loaderType) {
            LoaderType.LANDING -> {
                accountOptionsManageCardFragmentBinding.cardShimmer.loadingState(
                    loading.isLoading,
                    shimmerContainer = accountOptionsManageCardFragmentBinding.rltCardShimmer
                )
            }
            else -> cardFreezeViewModel.stopLoading()
        }
    }

    override fun onResume() {
        super.onResume()

        if (SHOW_GET_REPLACEMENT_CARD_SCREEN) {
            SHOW_GET_REPLACEMENT_CARD_SCREEN = false
            viewLifecycleOwner.lifecycleScope.launch {
                router.navigateToGetReplacementCard(requireActivity())
            }
        } else if (SHOW_ACTIVATE_VIRTUAL_CARD_SCREEN) {
            SHOW_ACTIVATE_VIRTUAL_CARD_SCREEN = false
            viewLifecycleOwner.lifecycleScope.launch {
                router.navigateToTemporaryStoreCard(requireActivity())
            }
        }
    }
}
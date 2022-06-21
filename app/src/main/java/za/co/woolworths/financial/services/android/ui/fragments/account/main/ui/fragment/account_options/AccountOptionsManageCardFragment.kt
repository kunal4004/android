package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsManageCardFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.LoaderType
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card.ManageCardItemListener
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card.ManageCardLandingHeaderItems
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card.ManageStoreCardLandingList
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.StorCardCallBack
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.CallBack
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import javax.inject.Inject

@AndroidEntryPoint
class AccountOptionsManageCardFragment : Fragment(R.layout.account_options_manage_card_fragment) {

    companion object {
        const val MANAGE_CARD_ACCOUNT_OPTIONS = "AccountOptionsManageCardFragment"
    }

    private lateinit var mOnItemClickListener: ManageCardItemListener
    private lateinit var mHeaderItems: ManageCardLandingHeaderItems
    private lateinit var mItemList: ManageStoreCardLandingList

    @Inject
    lateinit var router: ProductLandingRouterImpl

    val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()
    private val cardFreezeViewModel: TemporaryFreezeCardViewModel by activityViewModels()
    private val activityLauncher = BetterActivityResult.registerActivityForResult(this)

    private val landingController by lazy { (requireActivity() as? StoreCardActivity)?.landingNavController() }
    private lateinit var locator: Locator

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
        mOnItemClickListener =
            ManageCardItemListener(requireActivity(), router, includeListOptions).apply {
                onClickIntentObserver.observe(viewLifecycleOwner) {
                    when (it) {
                        is CallBack.IntentCallBack -> {
                            it.intent?.let { intent ->
                                storeCardLauncher(intent)
                            }
                        }
                        else->Unit
                    }
                }
            }
    }

    private fun storeCardLauncher(intent: Intent) {
        activityLauncher.launch(intent, onActivityResult = { result ->
            if (StorCardCallBack().linkNewCardCallBack(result)) {
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
        lifecycleScope.launch {
            with(viewModel) {
                requestGetStoreCardCards()
                storeCardResponseResult.collectLatest { response ->
                    with(response) {
                        locator.stopService()

                        renderNoConnection { router.showNoConnectionToast(requireActivity()) }

                        renderLoading {
                            when (viewModel.loaderType) {
                                LoaderType.LANDING -> {
                                    cardShimmer.loadingState(
                                        isLoading,
                                        shimmerContainer = rltCardShimmer
                                    )
                                }
                                else -> cardFreezeViewModel.stopLoading()
                            }
                        }

                        renderHttpFailureFromServer {
                            router.routeToServerErrorDialog(findNavController(), output.response)
                        }

                        renderFailure {
                            router.routeToDefaultErrorMessageDialog(
                                requireActivity(),
                                findNavController()
                            )
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.onCardTapEvent.collectLatest {
                landingController?.let { controller -> router.routeToManageMyCardDetails(controller) }
            }
        }

        lifecycleScope.launch {
            viewModel.onViewPagerPageChangeListener.collect { feature ->
                mHeaderItems.showHeaderItem(feature)
                mItemList.showListItem(feature)
            }
        }

    }
}
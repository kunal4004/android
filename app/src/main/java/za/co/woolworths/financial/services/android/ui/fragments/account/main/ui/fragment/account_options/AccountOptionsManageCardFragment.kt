package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options

import android.os.Bundle
import android.view.View
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
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderFailure
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderHttpFailureFromServer
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card.ManageCardItemListener
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card.ManageCardLandingHeaderItems
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card.ManageCardLandingItemList
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import javax.inject.Inject

@AndroidEntryPoint
class AccountOptionsManageCardFragment : Fragment(R.layout.account_options_manage_card_fragment) {

    companion object {
        const val MANAGE_CARD_ACCOUNT_OPTIONS = "AccountOptionsManageCardFragment"
    }

    private lateinit var mOnItemClickListener: ManageCardItemListener
    private lateinit var mHeaderItems: ManageCardLandingHeaderItems
    private lateinit var mItemList: ManageCardLandingItemList

    @Inject
    lateinit var router: ProductLandingRouterImpl

    val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()
    val cardFreezeViewModel: TemporaryFreezeCardViewModel by activityViewModels()

    private val landingController by lazy { (requireActivity() as? StoreCardActivity)?.landingNavController() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(AccountOptionsManageCardFragmentBinding.bind(view)) {
            mHeaderItems =
                ManageCardLandingHeaderItems(viewModel, this, this@AccountOptionsManageCardFragment)
            mItemList = ManageCardLandingItemList(
                cardFreezeViewModel,
                includeListOptions,
                this@AccountOptionsManageCardFragment
            )
            setOnClickListener()
            setupView()
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.setOnClickListener() {
        mOnItemClickListener = ManageCardItemListener(requireActivity(), router, includeListOptions)
        mOnItemClickListener.setOnClickListener()
    }

    private fun AccountOptionsManageCardFragmentBinding.setupView() {
        mItemList.hideAllRows()
        setupViewPager()
        setCardLabel()
        subscribeObservers()
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
                queryServiceGetStoreCardCards()
                storeCardResponseResult.collectLatest { response ->
                    with(response) {

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


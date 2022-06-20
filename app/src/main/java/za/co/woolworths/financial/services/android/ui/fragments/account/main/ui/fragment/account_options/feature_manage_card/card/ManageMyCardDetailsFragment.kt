package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ManageCardDetailsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.StorCardCallBack
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager
import javax.inject.Inject

@AndroidEntryPoint
class ManageMyCardDetailsFragment : Fragment(R.layout.manage_card_details_fragment) {

    private var mBindCardInfo: BindCardInfoTypeComponent? = null
    private var mOnItemClickListener: ManageCardItemListener? = null
    private var mItemList: ManageCardLandingItemList? = null
    val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()
    val cardFreezeViewModel: TemporaryFreezeCardViewModel by activityViewModels()

    @Inject lateinit var manageCardAdapter: ManageCardViewPagerAdapter

    @Inject lateinit var router: ProductLandingRouterImpl
    private val activityLauncher = BetterActivityResult.registerActivityForResult(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbar()
        with(ManageCardDetailsFragmentBinding.bind(view)) {
            setupView()
            mBindCardInfo = BindCardInfoTypeComponent(incManageCardDetailsInfoLayout,viewModel)
            mItemList = ManageCardLandingItemList(
                cardFreezeViewModel,
                includeListOptions,
                this@ManageMyCardDetailsFragment
            )
            setCardViewPagerNavigationGraph()
            setOnClickListener()
        }

        onBackPressed()
    }

    private fun setToolbar() {
        (activity as? StoreCardActivity)?.getToolbarHelper()
            ?.setManageMyCardDetailsToolbar(viewModel.dataSource.isMultipleStoreCardEnabled())
    }

    private fun setCardViewPagerNavigationGraph() = setupGraph(
        R.navigation.account_options_manage_card_nav,
        R.id.manageCardFragmentContainerView,
        R.id.manageCardViewPagerFragment
    )

    fun onBackPressed() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() { findNavController().popBackStack() } }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun ManageCardDetailsFragmentBinding.setOnClickListener() {
        mOnItemClickListener = ManageCardItemListener(requireActivity(), router, includeListOptions)
        mOnItemClickListener!!.command.observe(viewLifecycleOwner) {
            when(it!=null){
                true->{storeCardLauncher(it)}
            }
        }
    }
    private fun storeCardLauncher(intent: Intent) {
        activityLauncher.launch(intent, onActivityResult = { result ->
            when (StorCardCallBack().linkNewCardCallBack(result)) {
                true -> {
                    if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                        viewModel.queryServiceGetStoreCardCards()
                    } else {
                        ErrorHandlerView(activity).showToast()
                    }
                }
            }
        })
        activity?.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
    }
    private fun setupView() {
        mBindCardInfo?.initView()
        mItemList?.hideAllRows()
        subscribeObservers()
    }
    
    private fun subscribeObservers() {
        lifecycleScope.launch {
            viewModel.onViewPagerPageChangeListener.collect { feature ->
                mItemList?.showListItem(feature)
                mBindCardInfo?.setupView(feature.first)
            }
        }
    }

}

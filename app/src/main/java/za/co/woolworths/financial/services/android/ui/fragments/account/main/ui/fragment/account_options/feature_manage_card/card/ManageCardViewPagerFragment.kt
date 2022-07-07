package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ManageCardViewpagerFragmentBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.LoaderType
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.StoreCardNotReceivedDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.disableNestedScrolling
import za.co.woolworths.financial.services.android.util.Utils

@AndroidEntryPoint
class ManageCardViewPagerFragment : Fragment(R.layout.manage_card_viewpager_fragment) {

   private var manageCardAdapter: ManageCardViewPagerAdapter? = null

    val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()
    val cardFreezeViewModel: TemporaryFreezeCardViewModel by activityViewModels()
    var binding : ManageCardViewpagerFragmentBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ManageCardViewpagerFragmentBinding.bind(view)

        with(binding) {
            manageCardAdapter = ManageCardViewPagerAdapter(this@ManageCardViewPagerFragment)
            initCardViewPager()
            subscribeObservers()
        }
    }

    private fun ManageCardViewpagerFragmentBinding?.subscribeObservers() {
        lifecycleScope.launch {
            with(viewModel) {
                storeCardResponseResult.collectLatest { response ->
                    with(response) {
                        renderSuccess {
                            val currentPosition  = cardFreezeViewModel.currentPagePosition.value ?: 0
                            val listOfStoreCardFeatures = handleStoreCardResponseResult(output)
                            manageCardAdapter?.setItem(listOfStoreCardFeatures)
                            setDotIndicatorVisibility(listOfStoreCardFeatures)
                            viewModel.onCardPagerPageSelected(listOfStoreCardFeatures?.get(currentPosition),currentPosition)
                            viewModel.loaderType = LoaderType.LANDING
                            cardFreezeViewModel.stopLoading()
                        }
                    }
                }
            }
        }
    }

    private fun ManageCardViewpagerFragmentBinding?.setDotIndicatorVisibility(items: MutableList<StoreCardFeatureType>?) {
        if ((items?.size ?: 0) <= 1){
            this?.cardTabLayout?.visibility = GONE
            this?.tabHiddenMargin?.visibility = VISIBLE
        }else {
            this?.cardTabLayout?.visibility = VISIBLE
            this?.tabHiddenMargin?.visibility = GONE
        }

    }

    private fun ManageCardViewpagerFragmentBinding?.initCardViewPager() {
        val dimens = resources.getDimension(R.dimen._15sdp).toInt()
        this?.cardItemViewPager?.apply {
            disableNestedScrolling()
            offscreenPageLimit = 3
           setPageTransformer(OffsetPageTransformer(dimens, dimens))
            adapter = manageCardAdapter

            TabLayoutMediator(cardTabLayout, this) { _, _ -> }.attach()

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val listOfPrimaryStoreCards = manageCardAdapter?.getListOfStoreCards()
                    if ((listOfPrimaryStoreCards?.size ?: 0) > 0) {
                        cardFreezeViewModel.currentPagePosition.value = position
                        viewModel.onCardPagerPageSelected(listOfPrimaryStoreCards?.get(position),position)
                    }
                }
            })
        }

        manageCardAdapter?.setItem(viewModel.listOfStoreCardFeatureType)
        setDotIndicatorVisibility(viewModel.listOfStoreCardFeatureType)
        val defaultPosition = cardFreezeViewModel.currentPagePosition.value ?: 0
        this?.cardItemViewPager?.setCurrentItem(defaultPosition, true)
    }

}
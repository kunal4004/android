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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.LoaderType
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.BlockStoreCardType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.disableNestedScrolling
import za.co.woolworths.financial.services.android.util.voc.VoiceOfCustomerManager

@AndroidEntryPoint
class ManageCardViewPagerFragment : Fragment(R.layout.manage_card_viewpager_fragment) {

    var manageCardAdapter: ManageCardViewPagerAdapter?  = null
    val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()
    val cardFreezeViewModel: TemporaryFreezeCardViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ManageCardViewpagerFragmentBinding.bind(view)
        manageCardAdapter = ManageCardViewPagerAdapter(fragmentManager = childFragmentManager, lifecycle = lifecycle)
        with(binding) {
            initCardViewPager()
            subscribeObservers()
        }
    }

    private fun ManageCardViewpagerFragmentBinding?.subscribeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            with(viewModel) {
                storeCardResponseResult.collectLatest { response ->
                    with(response) {
                        renderSuccess {
                            val listOfStoreCardFeatures = handleStoreCardResponseResult(output)
                            manageCardAdapter?.setItem(listOfStoreCardFeatures)
                            setDotIndicatorVisibility(listOfStoreCardFeatures)
                            val currentPosition = getCardPosition(listOfStoreCardFeatures)
                            if (listOfStoreCardFeatures?.isNotEmpty() == true) {
                                onPagerSelected(listOfStoreCardFeatures,currentPosition, isPopupVisibleInAccountLanding = false, isPopupVisibleInCardDetailLanding = false)
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                VoiceOfCustomerManager().showPendingSurveyIfNeeded(requireContext())
                                handleBlockUnBlockStoreCardResult()
                            }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            cardFreezeViewModel.mStoreCardUpsellMessageFlagState.observeResult(viewLifecycleOwner){
                this@subscribeObservers?.cardItemViewPager?.setCurrentItem(0, true)
            }
        }
    }

    private fun getCardPosition(listOfStoreCardFeatures: MutableList<StoreCardFeatureType>?): Int =
        if ((listOfStoreCardFeatures?.size ?: 0) == 1) {
            cardFreezeViewModel.resetCardPosition()
            0
        } else cardFreezeViewModel.currentPagePosition.value ?: 0

    private fun handleBlockUnBlockStoreCardResult() {
        viewModel.loaderType = LoaderType.LANDING

        when (val type = cardFreezeViewModel.mStoreCardType) {
            is StoreCardType.PrimaryCard -> {
                if (type.block == BlockStoreCardType.FREEZE) {
                    cardFreezeViewModel.mStoreCardUpsellMessageFlagState.activateVirtualCardFlag()
                    cardFreezeViewModel.showToastMessageOnStoreCardFreeze.value = R.string.card_temporarily_frozen_label
                } else {
                    cardFreezeViewModel.showToastMessageOnStoreCardFreeze.value = R.string.card_temporarily_unfrozen_label
                }
            }
            else -> Unit
        }

        cardFreezeViewModel.stopLoading()
        viewModel.mStoreCardType = StoreCardType.None
        cardFreezeViewModel.mStoreCardType = StoreCardType.None
    }

    private fun ManageCardViewpagerFragmentBinding?.setDotIndicatorVisibility(items: MutableList<StoreCardFeatureType>?) {
            this?.dotIndicatorRelativeLayout?.visibility =  if ((items?.size ?: 0) <= 1) GONE else VISIBLE
    }

    private fun ManageCardViewpagerFragmentBinding.initCardViewPager() {
        val dimens = resources.getDimension(R.dimen._15sdp).toInt()
        with(cardItemViewPager) {
            disableNestedScrolling()
            offscreenPageLimit = 2
            adapter = manageCardAdapter
            setPageTransformer(OffsetPageTransformer(dimens, dimens))
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when(position){
                        0 -> {
                            dotAtPosition0Img.setImageResource(R.drawable.dot_selected)
                            dotAtPosition1Img.setImageResource(R.drawable.default_dot)
                        }
                        1 -> {
                            dotAtPosition0Img.setImageResource(R.drawable.default_dot)
                            dotAtPosition1Img.setImageResource(R.drawable.dot_selected)
                        }
                    }
                    onPagerSelected(
                        position = position,
                        isPopupVisibleInAccountLanding = true,
                        isPopupVisibleInCardDetailLanding = true
                    )

                }
            })

            dotAtPosition0Img.setOnClickListener { cardItemViewPager.setCurrentItem(0, true) }
            dotAtPosition1Img.setOnClickListener { cardItemViewPager.setCurrentItem(1, true) }

        }
        manageCardAdapter?.setItem(viewModel.listOfStoreCardFeatureType)
        setDotIndicatorVisibility(viewModel.listOfStoreCardFeatureType)

        with(cardItemViewPager) {
                when (val pagerPosition = cardFreezeViewModel.currentPagePosition.value ?: 0) {
                    0 -> {
                        post {
                            beginFakeDrag()
                            fakeDragBy(1.0f)
                            endFakeDrag()
                        }
                        setCurrentItem(pagerPosition, false)
                    }
                    else -> setCurrentItem(pagerPosition, false)
            }
        }
    }

    private fun onPagerSelected(
        listOfStoreCardFeatures: MutableList<StoreCardFeatureType>? = null,
        position: Int,
        isPopupVisibleInAccountLanding: Boolean,
        isPopupVisibleInCardDetailLanding: Boolean
    ) {
       val cardList  = listOfStoreCardFeatures ?:  manageCardAdapter?.getListOfStoreCards() ?: mutableListOf()
        if (cardList.size > 0) {
            cardFreezeViewModel.currentPagePosition.value = position
            viewModel.onManageCardPagerFragmentSelected(
                cardList[position],
                position,
                isPopupVisibleInAccountLanding = isPopupVisibleInAccountLanding,
                isPopupVisibleInCardDetailLanding = isPopupVisibleInCardDetailLanding
            )
        }
    }
}
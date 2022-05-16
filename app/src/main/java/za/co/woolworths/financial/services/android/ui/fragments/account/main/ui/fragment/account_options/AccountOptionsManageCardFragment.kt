package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsManageCardFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider.CardViewPager
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider.ManageCardScreenSlidesAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject


@AndroidEntryPoint
class AccountOptionsManageCardFragment :
    ViewBindingFragment<AccountOptionsManageCardFragmentBinding>(
        AccountOptionsManageCardFragmentBinding::inflate
    ) {

    @Inject
    lateinit var adapter: ManageCardScreenSlidesAdapter

    private val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pager = CardViewPager()

        setupCard(pager, items = mutableListOf(StoreCardFeatureType.OnStart)) { feature ->
            binding.showStoreCardItems(feature)
            dotIndicatorVisibility(adapter.getListOfStoreCards())
        }

        lifecycleScope.launchWhenStarted {
            with(viewModel) {
                queryServiceGetStoreCardCards().collect { response ->
                    with(response) {
                        renderSuccess {
                            SaveResponseDao.setValue(
                                SessionDao.KEY.STORE_CARD_RESPONSE_PAYLOAD,
                                this.output
                            )
                            val listOfStoreCardFeatures =
                                storeCardDataSource.getStoreCardListByFeatureType()
                            adapter.setItem(listOfStoreCardFeatures)
                        }
                        renderFailure { Log.e("renderStatus", "renderFailure") }
                        renderEmpty { Log.e("renderStatus", "renderEmpty") }
                        renderLoading {
                            binding.cardShimmer.loadingState(isLoading,shimmerContainer = binding.rltCardShimmer)
                            when (isLoading) {
                                true -> {
                                    binding.isAllMenuListItemsVisible(true)
                                    binding.isTemporaryFreezeCardVisible(false)
                                    binding.isAccountOptionsDividerVisible(false)
                                }
                                false -> Unit
                            }
                        }
                    }
                }
            }
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.isAccountOptionsDividerVisible(isVisible: Boolean) {
        accountOptionsDividerView.visibility= if (isVisible) VISIBLE else GONE
    }
    private fun AccountOptionsManageCardFragmentBinding.isAllMenuListItemsVisible(isHidden: Boolean) {
        menuItem1RelativeLayout.visibility = if (isHidden) GONE else VISIBLE
        menuItem2RelativeLayout.visibility = if (isHidden) GONE else VISIBLE
    }

    private fun AccountOptionsManageCardFragmentBinding.isTemporaryFreezeCardVisible(isVisible: Boolean) {
        menuItem3RelativeLayout.visibility = if (isVisible) VISIBLE else GONE
        isAllMenuListItemsVisible(isVisible)
        isAccountOptionsDividerVisible(!isVisible)
    }

    private fun AccountOptionsManageCardFragmentBinding.showStoreCardItems(storeCardFeatureType: StoreCardFeatureType?) =
        CoroutineScope(Dispatchers.Main).launch {
            when (storeCardFeatureType) {
                is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive -> {
                    isAllMenuListItemsVisible(false)
                    cardLabelVisibility(false)
                    manageCardLabelVisibility(false, true)
                    setListItems1Info(R.string.replacement_card_label, R.drawable.icon_card)
                    setListItems2Info(R.string.link_new_card, R.drawable.link_icon)
                    setStoreCardTag(R.string.inactive, R.string.red_tag, isVisible = true)
                    isTemporaryFreezeCardVisible(false)
                }
                is StoreCardFeatureType.StoreCardIsTemporaryFreeze -> {
                    when(storeCardFeatureType.isStoreCardFrozen){
                        true -> {
                            manageCardLabelVisibility(true, true)
                            cardLabelVisibility(true)
                            setStoreCardTag(R.string.freeze_temp_label, R.string.orange_tag, false)
                            isTemporaryFreezeCardVisible(true)
                        }
                        false -> {

                        }
                    }
                }
                else -> {
                    isAllMenuListItemsVisible(true)
                    cardLabelVisibility(true)
                    setStoreCardTag(R.string.inactive, R.string.red_tag, false)
                }
            }
        }

    private fun AccountOptionsManageCardFragmentBinding.manageCardLabelVisibility(
        isVisible: Boolean,
        isLabelUnderline: Boolean = false
    ) {
        manageCardText.visibility = if (isVisible) VISIBLE else INVISIBLE
        manageCardText.paintFlags =
            if (isLabelUnderline) manageCardText.paintFlags or Paint.UNDERLINE_TEXT_FLAG else 0
    }

    private fun AccountOptionsManageCardFragmentBinding.cardLabelVisibility(isVisible: Boolean) {
        cardText.visibility = if (isVisible) VISIBLE else GONE
    }

    private fun AccountOptionsManageCardFragmentBinding.setStoreCardTag(
        @StringRes tagTitleId: Int,
        @StringRes tagColor: Int,
        isVisible: Boolean
    ) {
        if (isVisible) {
            storeCardTagTextView.text = bindString(tagTitleId)
            KotlinUtils.roundCornerDrawable(storeCardTagTextView, bindString(tagColor))
            storeCardTagTextView.visibility = VISIBLE
        } else {
            storeCardTagTextView.visibility = GONE
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.setListItems2Info(
        titleId: Int,
        iconId: Int
    ) {
        storeCardItem2TextView.text = getString(titleId)
        storeCardItem2ImageView.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                iconId
            )
        )
    }


    private fun AccountOptionsManageCardFragmentBinding.setListItems1Info(
        @StringRes titleId: Int,
        @DrawableRes iconId: Int
    ) {
        storeCardItem1TextView.text = getString(titleId)
        storeCardItem1ImageView.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                iconId
            )
        )
        storeCardItem1ImageView.alpha = 0.3f
    }

    private fun setupCard(
        cardViewPager: CardViewPager,
        items: MutableList<StoreCardFeatureType>?,
        onPageSwipeListener: (StoreCardFeatureType?) -> Unit
    ) {
        with(adapter) {
            setItem(items)
            binding.cardLabelVisibility(true)
            binding.manageCardLabelVisibility(true, true)
            dotIndicatorVisibility(getListOfStoreCards())
            cardViewPager.invoke(
                binding.accountCardViewPager,
                binding.tab,
                this,
                onPageSwipeListener
            )
        }
    }

    private fun dotIndicatorVisibility(items: MutableList<StoreCardFeatureType>?) {
        binding.tab.visibility = if (items?.size ?: 0 <= 1) INVISIBLE else VISIBLE
    }
}


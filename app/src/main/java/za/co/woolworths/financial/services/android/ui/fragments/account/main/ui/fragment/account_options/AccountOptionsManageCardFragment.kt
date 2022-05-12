package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options

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
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider.CardViewPager
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider.ManageCardScreenSlidesAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.DetermineCardToDisplay
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

        setupCard(pager, items = mutableListOf()) { storeCard ->
            binding.showItemsList(storeCard)
            dotIndicatorVisibility(adapter.getListOfStoreCards())
        }

        lifecycleScope.launchWhenStarted {
            with(viewModel) {
                queryServiceGetStoreCardCards().collect { response ->
                    with(response) {
                        renderSuccess {
                            storeCardDataSource.addCardToDisplayStateToPrimaryCardObject(this.output)
                            adapter.setItem(this.output.storeCardsData?.primaryCards)
                        }
                        renderFailure { Log.e("renderStatus", "renderFailure") }
                        renderEmpty { Log.e("renderStatus", "renderEmpty") }
                        renderLoading {
                            binding.cardShimmer.loadingState(isLoading,shimmerContainer = binding.rltCardShimmer)
                            when (isLoading) {
                                true -> binding.shouldHideAllListItems(true)
                                false -> Unit
                            }
                        }
                    }
                }
            }
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.shouldHideAllListItems(isHidden: Boolean) {
        menuItem1RelativeLayout.visibility = if (isHidden) GONE else VISIBLE
        menuItem2RelativeLayout.visibility = if (isHidden) GONE else VISIBLE
    }

    private fun AccountOptionsManageCardFragmentBinding.showItemsList(storeCard: StoreCard?) =
        CoroutineScope(Dispatchers.Main).launch {
            when (storeCard?.cardDisplay) {
                DetermineCardToDisplay.StoreCardIsInstantReplacementCardAndInactive -> {
                    shouldHideAllListItems(false)
                    manageCardAndCardLabel(false)
                    setListItems1Info(R.string.replacement_card_label, R.drawable.icon_card)
                    setListItems2Info(R.string.link_new_card, R.drawable.link_icon)
                    setStoreCardTag(R.string.inactive, R.string.red_tag, true)
                }
                else -> {
                    shouldHideAllListItems(true)
                    manageCardAndCardLabel(true)
                    setStoreCardTag(R.string.inactive, R.string.red_tag, false)

                }
            }
        }

    private fun AccountOptionsManageCardFragmentBinding.manageCardAndCardLabel(isVisible: Boolean) {
        cardText.visibility = if (isVisible) VISIBLE else GONE
        manageCardText.visibility = if (isVisible) VISIBLE else INVISIBLE

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
        items: MutableList<StoreCard>?,
        onPageSwipeListener: (StoreCard?) -> Unit
    ) {
        val storeCardsResponse: StoreCardsResponse? =
            SaveResponseDao.getValue(SessionDao.KEY.STORE_CARE_RESPONSE_PAYLOAD)
        storeCardsResponse?.apply {
            with(adapter) {
                setItem(items)
                binding.manageCardAndCardLabel(true)
                dotIndicatorVisibility(getListOfStoreCards())
                cardViewPager.invoke(
                    binding.accountCardViewPager,
                    binding.tab,
                    this,
                    onPageSwipeListener
                )
            }
        }
    }

    private fun dotIndicatorVisibility(items: MutableList<StoreCard>?) {
        binding.tab.visibility = if (items?.size ?: 0 <= 1) INVISIBLE else VISIBLE
    }
}


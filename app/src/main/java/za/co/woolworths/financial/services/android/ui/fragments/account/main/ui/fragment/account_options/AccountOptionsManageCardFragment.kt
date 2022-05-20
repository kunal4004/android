package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import za.co.woolworths.financial.services.android.ui.views.actionsheet.TemporaryFreezeCardFragment.Companion.TEMPORARY_FREEZE_CARD_FRAGMENT_CANCEL_RESULT
import za.co.woolworths.financial.services.android.ui.views.actionsheet.TemporaryFreezeCardFragment.Companion.TEMPORARY_FREEZE_CARD_FRAGMENT_CONFIRM_RESULT
import za.co.woolworths.financial.services.android.ui.views.actionsheet.TemporaryUnFreezeCardFragment.Companion.UN_FREEZE_TEMPORARY_CARD_CANCEL_RESULT
import za.co.woolworths.financial.services.android.ui.views.actionsheet.TemporaryUnFreezeCardFragment.Companion.UN_FREEZE_TEMPORARY_CARD_CONFIRM_RESULT
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

@AndroidEntryPoint
class AccountOptionsManageCardFragment :
    ViewBindingFragment<AccountOptionsManageCardFragmentBinding>(
        AccountOptionsManageCardFragmentBinding::inflate
    ) {

    companion object {
        const val MANAGE_CARD_ACCOUNT_OPTIONS =  "AccountOptionsManageCardFragment"
    }

    @Inject
    lateinit var adapter: ManageCardScreenSlidesAdapter

    private val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()
    private val pager by lazy { CardViewPager() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCardViewPager()
        binding.setupTemporaryFreezeCardSwipe()
        subscribeObservers()
        setResultListeners()
    }

    private fun setResultListeners() {
        setFragmentResultListener(MANAGE_CARD_ACCOUNT_OPTIONS) { _ , bundle ->
                when(bundle.getString(MANAGE_CARD_ACCOUNT_OPTIONS, "")){
                    TEMPORARY_FREEZE_CARD_FRAGMENT_CONFIRM_RESULT -> {
                        queryServiceBlockUnblockStoreCard(binding.accountCardViewPager.currentItem)
                    }

                    TEMPORARY_FREEZE_CARD_FRAGMENT_CANCEL_RESULT -> {
                        binding.isTemporaryCardSwitchChecked(false)
                    }

                    UN_FREEZE_TEMPORARY_CARD_CONFIRM_RESULT -> {
                        Log.e("blockwaor", "UN_FREEZE_TEMPORARY_CARD_CONFIRM_RESULT")

                    }
                    UN_FREEZE_TEMPORARY_CARD_CANCEL_RESULT -> {
                        binding.isTemporaryCardSwitchChecked(true)
                    }
                }
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.isTemporaryCardSwitchChecked(isChecked : Boolean) {
        switchTemporaryFreezeCard.isChecked = isChecked
    }

    private fun queryServiceBlockUnblockStoreCard(position : Int ) {
        lifecycleScope.launch {
            viewModel.queryServiceBlockUnblockStoreCard(position).collect {  state ->
                with(state){
                    renderLoading {
                        binding.cardShimmer.loadingState(isLoading,shimmerContainer = binding.rltCardShimmer)
                    }

                    renderSuccess {

                    }

                    renderFailure {
                        //Log.e("outputabc", Gson().toJson(output))
                    }
                }
            }
        }
    }

    private fun initCardViewPager() {
        setupCard(pager, items = mutableListOf(StoreCardFeatureType.OnStart)) { feature ->
            binding.showStoreCardItems(feature)
            dotIndicatorVisibility(adapter.getListOfStoreCards())
        }
    }

    private fun subscribeObservers() {
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
                                    binding.isManageCardVisible(false)
                                }
                                false -> Unit
                            }
                        }
                    }
                }
            }
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.isAllMenuListItemsVisible(isHidden: Boolean) {
//        menuItem1RelativeLayout.visibility = if (isHidden) GONE else VISIBLE
//        menuItem2RelativeLayout.visibility = if (isHidden) GONE else VISIBLE
        isLinkNewCardUIVisible(!isHidden)
        isActivateVirtualTempCardUIVisible(!isHidden)
        isInstantReplacementCardUIVisible(!isHidden)
        isMenuItem2UIVisible(!isHidden)
        isMenuItem4UIVisible(!isHidden)
        isTemporaryFreezeCardVisible(!isHidden)
        isManageCardVisible(!isHidden)
    }

    private fun AccountOptionsManageCardFragmentBinding.isTemporaryFreezeCardVisible(isVisible: Boolean) {
        temporaryFreezeCardDivider.visibility = if (isVisible) VISIBLE else GONE
        temporaryFreezeCardRelativeLayout.visibility = if (isVisible) VISIBLE else GONE
    }

    private fun AccountOptionsManageCardFragmentBinding.setupTemporaryFreezeCardSwipe() {
        switchTemporaryFreezeCard.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) { // block is active for user interaction only
                when (isChecked) {
                    true -> findNavController().navigate(AccountOptionsManageCardFragmentDirections.actionAccountOptionsManageCardFragmentToTemporaryFreezeCardFragment())
                    false -> findNavController().navigate(AccountOptionsManageCardFragmentDirections.actionAccountOptionsManageCardFragmentToTemporaryUnFreezeCardFragment())
                }
            }
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.showStoreCardItems(storeCardFeatureType: StoreCardFeatureType?) =
        CoroutineScope(Dispatchers.Main).launch {
            isAllMenuListItemsVisible(true)
            when (storeCardFeatureType) {

                is StoreCardFeatureType.ActivateVirtualTempCard -> {
                    isManageCardVisible(false)
                    myCardLabelVisibility(false)
                    manageCardLabelVisibility(isVisible = false, isLabelUnderline = true)
                    isActivateVirtualTempCardUIVisible(true)
                    isActivateVirtualTempCardUIVisible(true)
                    setBadge(R.string.inactive, R.string.red_tag, isVisible = true)
                    isLinkNewCardUIVisible(true)
                    isTemporaryFreezeCardVisible(false)
                }

                is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive -> {
                    myCardLabelVisibility(false)
                    manageCardLabelVisibility(isVisible = false, isLabelUnderline = true)
                    isInstantReplacementCardUIVisible(true)
                    isLinkNewCardUIVisible(true)
                    setBadge(R.string.inactive, R.string.red_tag, isVisible = true)
                    isTemporaryFreezeCardVisible(false)
                }
                is StoreCardFeatureType.StoreCardIsTemporaryFreeze -> {
                    when (storeCardFeatureType.isStoreCardFrozen) {
                        true -> {
                            manageCardLabelVisibility(isVisible = true, isLabelUnderline = true)
                            myCardLabelVisibility(true)
                            setBadge(R.string.freeze_temp_label, R.string.orange_tag, false)
                            isTemporaryFreezeCardVisible(true)
                            isManageCardVisible(false)
                            isTemporaryCardSwitchChecked(true)
                        }
                        false -> {

                        }
                    }
                }

                is StoreCardFeatureType.TemporaryCardEnabled -> {
                    setBadge(R.string.temp_card, R.string.orange_tag, true)
                    manageCardLabelVisibility(isVisible = false, isLabelUnderline = false)
                    myCardLabelVisibility(true)
                    isManageCardVisible(storeCardFeatureType.isBlockTypeNullInVirtualCardObject)
                    isLinkNewCardUIVisible(true)
                    isInstantReplacementCardUIVisible(false)
                }

                is StoreCardFeatureType.ManageMyCard -> {
                    setBadge(R.string.inactive, R.string.red_tag, false)
                    manageCardLabelVisibility(isVisible = false, isLabelUnderline = false)
                    isManageCardVisible(true)
                    myCardLabelVisibility(true)
                }

                else -> {}
            }
        }

    private fun AccountOptionsManageCardFragmentBinding.manageCardLabelVisibility(
        isVisible: Boolean,
        isLabelUnderline: Boolean = false
    ) {
        manageCardText.visibility = if (isVisible) VISIBLE else INVISIBLE
        manageCardText.paintFlags = if (isLabelUnderline) manageCardText.paintFlags or Paint.UNDERLINE_TEXT_FLAG else 0
    }

    private fun AccountOptionsManageCardFragmentBinding.myCardLabelVisibility(isVisible: Boolean) {
        cardText.visibility = if (isVisible) VISIBLE else GONE
    }

    private fun AccountOptionsManageCardFragmentBinding.setBadge(
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

    private fun setupCard(
        cardViewPager: CardViewPager,
        items: MutableList<StoreCardFeatureType>?,
        onPageSwipeListener: (StoreCardFeatureType?) -> Unit
    ) {
        with(adapter) {
            setItem(items)
            binding.myCardLabelVisibility(true)
            binding.manageCardLabelVisibility(isVisible = true, isLabelUnderline = true)
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

    private fun AccountOptionsManageCardFragmentBinding.isManageCardVisible(isVisible: Boolean) =
        isMenuItemVisible(manageCardDivider, manageCardRelativeLayout, isVisible)

    private fun AccountOptionsManageCardFragmentBinding.isInstantReplacementCardUIVisible(isVisible: Boolean) =
        isMenuItemVisible(replacementCardDivider, replacementCardRelativeLayout, isVisible)

    private fun AccountOptionsManageCardFragmentBinding.isLinkNewCardUIVisible(isVisible: Boolean) =
        isMenuItemVisible(linkNewCardDivider, linkNewCardRelativeLayout, isVisible)

    private fun AccountOptionsManageCardFragmentBinding.isActivateVirtualTempCardUIVisible(isVisible: Boolean) =
        isMenuItemVisible(
            activateVirtualTempCardDivider,
            activateVirtualTempCardRelativeLayout,
            isVisible
        )

    private fun AccountOptionsManageCardFragmentBinding.isMenuItem2UIVisible(isVisible: Boolean) =
        isMenuItemVisible(menuItem2Divider, menuItem2RelativeLayout, isVisible)

    private fun AccountOptionsManageCardFragmentBinding.isMenuItem4UIVisible(isVisible: Boolean) =
        isMenuItemVisible(menuItem4Divider, menuItem4RelativeLayout, isVisible)

    private fun isMenuItemVisible(
        divider: View,
        rootContainer: RelativeLayout,
        isVisible: Boolean
    ) =
        when (isVisible) {
            true -> {
                divider.visibility = VISIBLE
                rootContainer.visibility = VISIBLE
            }
            false -> {
                divider.visibility = GONE
                rootContainer.visibility = GONE
            }
        }
}


package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsManageCardFragmentBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.base.onClick
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeUnfreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider.CardViewPager
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider.ManageCardScreenSlidesAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

@AndroidEntryPoint
class AccountOptionsManageCardFragment : Fragment(R.layout.account_options_manage_card_fragment),
    OnClickListener {

    companion object {
        const val MANAGE_CARD_ACCOUNT_OPTIONS = "AccountOptionsManageCardFragment"
    }

    @Inject
    lateinit var adapter: ManageCardScreenSlidesAdapter

    @Inject
    lateinit var router: ProductLandingRouterImpl

    private val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()
    val temporaryFreezeUnfreezeCardViewModel: TemporaryFreezeUnfreezeCardViewModel by activityViewModels()

    private val pager by lazy { CardViewPager() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(AccountOptionsManageCardFragmentBinding.bind(view)) {
            initCardViewPager()
            subscribeObservers()
            setOnClickListener()
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.setOnClickListener() {
        manageCardRelativeLayout.onClick(this@AccountOptionsManageCardFragment)
        linkNewCardRelativeLayout.onClick(this@AccountOptionsManageCardFragment)
        activateVirtualTempCardRelativeLayout.onClick(this@AccountOptionsManageCardFragment)
        replacementCardRelativeLayout.onClick(this@AccountOptionsManageCardFragment)
        blockCardRelativeLayout.onClick(this@AccountOptionsManageCardFragment)
    }

    private fun AccountOptionsManageCardFragmentBinding.initCardViewPager() {
        setupCard(pager, items = mutableListOf(StoreCardFeatureType.OnStart)) { feature ->
            showStoreCardItems(feature)
            dotIndicatorVisibility(adapter.getListOfStoreCards())
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.subscribeObservers() {
        lifecycleScope.launchWhenStarted {
            with(viewModel) {
                queryServiceGetStoreCardCards().collect { response ->
                    with(response) {
                        renderSuccess {
                            SaveResponseDao.setValue(
                                SessionDao.KEY.STORE_CARD_RESPONSE_PAYLOAD,
                                this.output
                            )
                            val listOfStoreCardFeatures =storeCardDataSource.getStoreCardListByFeatureType()
                            adapter.setItem(listOfStoreCardFeatures)
                        }

                        renderHttpFailureFromServer {
                            Log.e(
                                "renderStatus",
                                "renderHttpFailureFromServer ${Gson().toJson(error)}"
                            )

                        }

                        renderFailure {
                            Log.e("renderStatus", "renderFailure")
                        }
                        renderEmpty { Log.e("renderStatus", "renderEmpty") }
                        renderLoading {
                            cardShimmer.loadingState(isLoading, shimmerContainer = rltCardShimmer)
                            when (isLoading) {
                                true -> {
                                    isAllMenuListItemsVisible(true)
                                    isBlockCardUIVisible(false)
                                    isTemporaryFreezeCardVisible(false)
                                    isManageCardVisible(false)
                                }
                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.isAllMenuListItemsVisible(isHidden: Boolean) {
        isLinkNewCardUIVisible(!isHidden)
        isActivateVirtualTempCardUIVisible(!isHidden)
        isInstantReplacementCardUIVisible(!isHidden)
        isMenuItem2UIVisible(!isHidden)
        isMenuItem4UIVisible(!isHidden)
        isTemporaryFreezeCardVisible(!isHidden)
        isManageCardVisible(!isHidden)
        isBlockCardUIVisible(!isHidden)
    }

    private fun AccountOptionsManageCardFragmentBinding.isTemporaryFreezeCardVisible(isVisible: Boolean) {
        temporaryFreezeCardFragmentContainerView.visibility = if (isVisible) VISIBLE else GONE
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
                            isTemporaryFreezeCardVisible(true)
                            setBadge(R.string.freeze_temp_label, R.string.orange_tag, false)
                            myCardLabelVisibility(true)
                            manageCardLabelVisibility(isVisible = true, isLabelUnderline = true)
                            isTemporaryCardSwitchChecked(false)
                            isBlockCardUIVisible(true)
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

                else -> {
                }
            }
        }

    private fun isTemporaryCardSwitchChecked(isSwitcherEnabled: Boolean) {
        temporaryFreezeUnfreezeCardViewModel.isSwitcherEnabled.value = isSwitcherEnabled
    }

    private fun AccountOptionsManageCardFragmentBinding.manageCardLabelVisibility(
        isVisible: Boolean,
        isLabelUnderline: Boolean = false
    ) {
        manageCardText.visibility = if (isVisible) VISIBLE else INVISIBLE
        manageCardText.paintFlags =
            if (isLabelUnderline) manageCardText.paintFlags or Paint.UNDERLINE_TEXT_FLAG else 0
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

    private fun AccountOptionsManageCardFragmentBinding.setupCard(
        cardViewPager: CardViewPager,
        items: MutableList<StoreCardFeatureType>?,
        onPageSwipeListener: (StoreCardFeatureType?) -> Unit
    ) {
        with(adapter) {
            setItem(items)
            myCardLabelVisibility(true)
            manageCardLabelVisibility(isVisible = true, isLabelUnderline = true)
            dotIndicatorVisibility(getListOfStoreCards())
            cardViewPager.invoke(
                accountCardViewPager,
                tab,
                this,
                onPageSwipeListener
            )
        }
    }

    private fun AccountOptionsManageCardFragmentBinding.dotIndicatorVisibility(items: MutableList<StoreCardFeatureType>?) {
        tab.visibility = if (items?.size ?: 0 <= 1) INVISIBLE else VISIBLE
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

    private fun AccountOptionsManageCardFragmentBinding.isBlockCardUIVisible(isVisible: Boolean) =
        isMenuItemVisible(
            blockCardDivider,
            blockCardRelativeLayout,
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

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.manageCardRelativeLayout -> router.routeToManageMyCard(requireActivity())
            R.id.linkNewCardRelativeLayout -> router.routeToLinkNewCard(requireActivity())
            R.id.activateVirtualTempCardRelativeLayout -> router.routeToActivateVirtualTempCard(requireActivity())
            R.id.replacementCardRelativeLayout -> router.routeToGetReplacementCard(requireActivity())
            R.id.blockCardRelativeLayout -> router.routeToBlockCard(requireActivity())
        }
    }
}


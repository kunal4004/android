package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsManageCardListFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.ActionButton
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardItemActions
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.StoreCardInfo
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardEnhancementConstant
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.util.Utils

sealed class ListCallback {
    data class CardNotReceived(val isCardNotReceivedFlowNeeded: Boolean) : ListCallback()
}

class ManageStoreCardLandingList(
    val viewModel: MyAccountsRemoteApiViewModel,
    private val router: ProductLandingRouterImpl,
    private val cardFreezeViewModel: TemporaryFreezeCardViewModel,
    private val includeListOptions: AccountOptionsManageCardListFragmentBinding,
    private val fragment: Fragment?
) {

    fun hideAllRows() {
        with(includeListOptions) {
            temporaryFreezeCardFragmentContainerView.visibility = GONE
            manageCardDivider.visibility = GONE
            manageCardRelativeLayout.visibility = GONE
            replacementCardDivider.visibility = GONE
            replacementCardRelativeLayout.visibility = GONE
            activateVirtualTempCardDivider.visibility = GONE
            activateVirtualTempCardRelativeLayout.visibility = GONE
            linkNewCardDivider.visibility = GONE
            linkNewCardRelativeLayout.visibility = GONE
            blockCardDivider.visibility = GONE
            blockCardRelativeLayout.visibility = GONE
            payWithCardFragmentContainerView.visibility = GONE
            storeCardRowContainer.visibility = GONE
            includeListOptions.parentLinearLayout.removeAllViews()
            parentLinearLayout.visibility = GONE
        }
    }

    fun showListItem(
        storeCardFeatureType: StoreCardInfo,
        callback: (ListCallback) -> Unit
    ) {
        fragment?.viewLifecycleOwner?.lifecycleScope?.launch(Dispatchers.Main) {
            hideAllRows()
            when (val featureType = storeCardFeatureType.feature) {

                    is StoreCardFeatureType.StoreCardFreezeCardUpShellMessage,
                    is StoreCardFeatureType.StoreCardActivateVirtualTempCardUpShellMessage -> {
                        hideAllRows()
                        includeListOptions.parentLinearLayout.removeAllViewsInLayout()
                    }

                    is StoreCardFeatureType.ActivateVirtualTempCard -> {
                        if (actionForStoreCardUsage1Item(
                                featureType.storeCard,
                                callback
                            )
                        ) return@launch
                        showActivateVirtualTempCardRow(featureType.isTemporaryCardEnabled)
                    }

                    is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive -> {
                        if (actionForStoreCardUsage1Item(
                                featureType.storeCard,
                                callback
                            )
                        ) return@launch
                        showInstantReplacementCardAndInactive()
                    }

                    is StoreCardFeatureType.StoreCardIsTemporaryFreeze -> {
                        showStoreCardIsTemporaryFreeze(featureType)
                        if (actionForStoreCardUsage1Item(
                                featureType.storeCard,
                                callback
                            )
                        ) return@launch
                    }

                    is StoreCardFeatureType.TemporaryCardEnabled -> {
                        if (actionForStoreCardUsage1Item(
                                featureType.storeCard,
                                callback
                            )
                        ) return@launch
                        showTemporaryCardEnabled(featureType.storeCard, callback)
                    }

                    is StoreCardFeatureType.ManageMyCard -> {
                        if (actionForStoreCardUsage1Item(
                                featureType.storeCard,
                                callback
                            )
                        ) return@launch
                        showManageMyCardRow()
                    }

                    else -> Unit

            }
        }
    }

    private fun showManageMyCardRow() {
        includeListOptions.manageCardDivider.visibility = VISIBLE
        showInstantReplacementCardAndInactive()
    }

    private fun showTemporaryCardEnabled(
        storeCard: StoreCard?,
        callback: (ListCallback) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            includeListOptions.payWithCardFragmentContainerView.visibility = VISIBLE
        }
        callback(
            ListCallback.CardNotReceived(isCardNotReceivedFlowNeeded = cardFreezeViewModel.isCardNotReceivedFlowNeeded(storeCard))
        )
    }

    fun setupVirtualTemporaryCardGraph() {
        fragment?.setupGraph(
            R.navigation.account_options_manage_card_nav,
            R.id.payWithCardFragmentContainerView,
            R.id.payWithCardListFragment
        )
    }

    fun setupTemporaryFreezeCardGraph() {
        fragment?.setupGraph(
            R.navigation.freeze_unfreeze_card_item_nav,
            R.id.temporaryFreezeCardFragmentContainerView,
            R.id.temporaryFreezeUnfreezeCardItemFragment
        )
    }

    private fun showStoreCardIsTemporaryFreeze(featureType: StoreCardFeatureType.StoreCardIsTemporaryFreeze) {
        if (!featureType.storeCard?.blockType.equals(
                StoreCardEnhancementConstant.NewCard,
                ignoreCase = true
            )
        ) {
            with(includeListOptions) {
                temporaryFreezeCardFragmentContainerView.visibility = VISIBLE
                when (featureType.isStoreCardFrozen) {
                    true -> cardFreezeViewModel.isSwitcherEnabled.value = true
                    else -> {
                        cardFreezeViewModel.isSwitcherEnabled.value = false
                        blockCardDivider.visibility = VISIBLE
                        blockCardRelativeLayout.visibility = VISIBLE
                    }
                }
            }
        }
    }

    private fun showInstantReplacementCardAndInactive() {
        with(includeListOptions) {
            replacementCardDivider.visibility = VISIBLE
            replacementCardRelativeLayout.visibility = VISIBLE

            showLinkNewCardItem(true)
        }
    }

    private fun showActivateVirtualTempCardRow(isTemporaryCardEnabled: Boolean) {
           showActivateVirtualCardItem(true)
           showLinkNewCardItem(!isTemporaryCardEnabled)
    }

    fun showCardNotReceivedDialog(viewModel: MyAccountsRemoteApiViewModel,displayCardNotReceivedPopup : () -> Unit) {
        if (viewModel.isStoreCardNotReceivedDialogFragmentVisible) return
        displayCardNotReceivedPopup()
    }

    private fun actionForStoreCardUsage1Item(
        storeCard: StoreCard?,
        callback: (ListCallback) -> Unit
    ): Boolean {
            includeListOptions.storeCardRowContainer.visibility = VISIBLE
            includeListOptions.parentLinearLayout.visibility = VISIBLE
            return storeCard?.actions?.let { action ->
                action.forEach { actionButton ->
                    when (actionButton.action) {
                        StoreCardItemActions.LINK_STORE_CARD -> addViewToViewGroup(
                            actionButton,
                            R.drawable.link_icon
                        )

                        StoreCardItemActions.ACTIVATE_VIRTUAL_CARD -> addViewToViewGroup(
                            actionButton,
                            R.drawable.ic_replacement_card_icon)

                        StoreCardItemActions.CARD_REPLACEMENT -> addViewToViewGroup(
                            actionButton,
                            R.drawable.ic_replacement_card_icon)

                        StoreCardItemActions.PAY_WITH_CARD -> showTemporaryCardEnabled(
                            storeCard,
                            callback
                        )

                        StoreCardItemActions.HOW_IT_WORKS -> addViewToViewGroup(
                            actionButton,
                            R.drawable.ic_how_to_pay)

                        else -> Unit
                    }
                }
                true
            } ?: run { false }

    }

    private fun showLinkNewCardItem(isVisible: Boolean, label: String? = null) {
        with(includeListOptions) {
           label?.let { linkNewCardTextView.text = it }
            when (isVisible) {
                true -> {
                    linkNewCardDivider.visibility = VISIBLE
                    linkNewCardRelativeLayout.visibility = VISIBLE
                }
                false -> {
                    linkNewCardDivider.visibility = GONE
                    linkNewCardRelativeLayout.visibility = GONE
                }
            }
        }
    }

    private fun showActivateVirtualCardItem(isVisible : Boolean, label : String? = null){
        with(includeListOptions) {
            label?.let { activateVirtualTempCardTextView.text = it }
            when (isVisible) {
                true -> {
                    activateVirtualTempCardDivider.visibility = VISIBLE
                    activateVirtualTempCardRelativeLayout.visibility = VISIBLE
                }
                false -> {
                    activateVirtualTempCardDivider.visibility = GONE
                    activateVirtualTempCardRelativeLayout.visibility = GONE
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun addViewToViewGroup(actionButton: ActionButton, @DrawableRes drawableId : Int) {
        includeListOptions.parentLinearLayout.apply {
            val layoutInflater = LayoutInflater.from(this.context)
            val inflater = layoutInflater.inflate(R.layout.store_card_list_item, null)
            val label = actionButton.label
            val action = actionButton.action
            val titleTextView = inflater.findViewById<TextView>(R.id.titleTextView)
            val rootLayout = inflater.findViewById<RelativeLayout>(R.id.linkNewCardRelativeLayout)
            val logoImageView = inflater.findViewById<ImageView>(R.id.logoImageView)
            logoImageView.setImageResource(drawableId)
            logoImageView.alpha = 0.3f
            rootLayout.setOnClickListener {
                when(action) {
                    StoreCardItemActions.LINK_STORE_CARD -> includeListOptions.linkNewCardRelativeLayout.performClick()
                    StoreCardItemActions.ACTIVATE_VIRTUAL_CARD -> includeListOptions.activateVirtualTempCardRelativeLayout.performClick()
                    StoreCardItemActions.CARD_REPLACEMENT -> includeListOptions.replacementCardRelativeLayout.performClick()
                    StoreCardItemActions.HOW_IT_WORKS -> {
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_HOW_TO,
                            fragment?.requireActivity()
                        )
                        router.routeToHowItWorks(
                            fragment?.requireActivity(),
                            viewModel.dataSource.isStaffMemberAndHasTemporaryCard(),
                            viewModel.dataSource.getVirtualCardStaffMemberMessage()
                        )
                    }
                    else -> Unit
                }
            }
            titleTextView.text = label
            addView(inflater)
        }
    }


}

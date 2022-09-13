package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsManageCardListFragmentBinding
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardItemActions
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.StoreCardInfo
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.StoreCardNotReceivedDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardUpShellMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.util.FirebaseManager


sealed class ListCallback {
    data class CardNotReceived(val isCardNotReceived: Boolean) : ListCallback()
}

class ManageStoreCardLandingList(
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
        }
    }

    fun showListItem(
        storeCardFeatureType: StoreCardInfo,
        callback: (ListCallback) -> Unit
    ) {
        fragment?.viewLifecycleOwner?.lifecycleScope?.launch {
            hideAllRows()
            when (val featureType = storeCardFeatureType.feature) {

                is StoreCardFeatureType.StoreCardFreezeCardUpShellMessage,
                is StoreCardFeatureType.StoreCardActivateVirtualTempCardUpShellMessage -> {
                    hideAllRows()
                }

                is StoreCardFeatureType.ActivateVirtualTempCard -> {
                    if (usage1FeatureContainsAction(featureType.storeCard)) return@launch
                    showActivateVirtualTempCardRow(featureType.isTemporaryCardEnabled)
                }

                is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive -> {
                    if (usage1FeatureContainsAction(featureType.storeCard)) return@launch
                    showInstantReplacementCardAndInactive()
                }

                is StoreCardFeatureType.StoreCardIsTemporaryFreeze -> {
                    if (usage1FeatureContainsAction(featureType.storeCard)) return@launch
                    showStoreCardIsTemporaryFreeze(featureType)
                }

                is StoreCardFeatureType.TemporaryCardEnabled -> {
                    if (usage1FeatureContainsAction(featureType.storeCard)) return@launch
                    showTemporaryCardEnabled(featureType, callback)
                }

                StoreCardFeatureType.ManageMyCard ->
                    showManageMyCardRow()

                else -> Unit

            }
        }
    }

    private fun showManageMyCardRow() {
        includeListOptions.manageCardDivider.visibility = VISIBLE
        showInstantReplacementCardAndInactive()
    }

    private fun showTemporaryCardEnabled(
        featureType: StoreCardFeatureType.TemporaryCardEnabled,
        callback: (ListCallback) -> Unit
    ) {
        includeListOptions.payWithCardFragmentContainerView.visibility = VISIBLE
        callback(
            ListCallback.CardNotReceived(
                isCardNotReceived = cardFreezeViewModel.isCardNotReceived(
                    featureType.storeCard
                )
            )
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

            when (featureType.upShellMessage) {
                is StoreCardUpShellMessage.ActivateVirtualTempCard -> {
                    activateVirtualTempCardDivider.visibility = VISIBLE
                    activateVirtualTempCardRelativeLayout.visibility = VISIBLE
                }
                else -> {
                    activateVirtualTempCardDivider.visibility = GONE
                    activateVirtualTempCardRelativeLayout.visibility = GONE
                }
            }
        }
    }

    private fun showInstantReplacementCardAndInactive() {
        with(includeListOptions) {
            replacementCardDivider.visibility = VISIBLE
            replacementCardRelativeLayout.visibility = VISIBLE

            showLinkNewCardItem(true, null)
        }
    }

    private fun showActivateVirtualTempCardRow(isTemporaryCardEnabled: Boolean) {
        with(includeListOptions) {
            activateVirtualTempCardDivider.visibility = VISIBLE
            activateVirtualTempCardRelativeLayout.visibility = VISIBLE

            if (isTemporaryCardEnabled) {
                showLinkNewCardItem(false,null)

            } else {
                showLinkNewCardItem(true,null)

            }

        }
    }

    fun showCardNotReceivedDialog(fragment: Fragment?, viewModel: MyAccountsRemoteApiViewModel) {
        if (viewModel.isStoreCardNotReceivedDialogFragmentVisible) return
        val dialog = StoreCardNotReceivedDialogFragment.newInstance()
        try {
            fragment?.childFragmentManager?.let {
                dialog.show(
                    it,
                    StoreCardNotReceivedDialogFragment::class.java.simpleName
                )
            }
        } catch (e: IllegalStateException) {
            FirebaseManager.logException(e)
        }
    }

    private fun usage1FeatureContainsAction(storeCard: StoreCard?): Boolean {
        return storeCard?.actions?.let { action ->
            action.forEach {  actionButton ->
                when(actionButton.action){
                    StoreCardItemActions.LINK_STORE_CARD -> showLinkNewCardItem(true, actionButton.label)
                    StoreCardItemActions.ACTIVATE_VIRTUAL_CARD -> showActivateVirtualCardItem(true, actionButton.label)
                    null -> Unit
                }
            }
            true
        } ?: run { false }
    }

    private fun showLinkNewCardItem(isVisible: Boolean, label: String?) {
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

    private fun showActivateVirtualCardItem(isVisible : Boolean, label : String?){
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

}

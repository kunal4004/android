package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsManageCardListFragmentBinding
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.StoreCardInfo
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.StoreCardNotReceivedDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.util.FirebaseManager

sealed class ListCallback {
    data class CardNotReceived (val isCardNotReceived: Boolean) : ListCallback()
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

                    hideAllRows() }

                is StoreCardFeatureType.ActivateVirtualTempCard ->
                    showActivateVirtualTempCardRow(featureType.isTemporaryCardEnabled)

                is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive ->
                    showInstantReplacementCardAndInactive()

                is StoreCardFeatureType.StoreCardIsTemporaryFreeze ->
                    showStoreCardIsTemporaryFreeze(featureType)

                is StoreCardFeatureType.TemporaryCardEnabled ->
                    showTemporaryCardEnabled(featureType, callback)

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
        callback: (ListCallback) -> Unit) {
        includeListOptions.payWithCardFragmentContainerView.visibility = VISIBLE
            callback(
                ListCallback.CardNotReceived(
                    isCardNotReceived = cardFreezeViewModel.isCardNotReceived(
                        featureType.storeCard
                    )
                )
            )
    }

    fun setupVirtualTemporaryCardGraph(){
        fragment?.setupGraph(
            R.navigation.account_options_manage_card_nav,
            R.id.payWithCardFragmentContainerView,
            R.id.payWithCardListFragment
        )
    }

        fun setupTemporaryFreezeCardGraph(){
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
        }
    }

    private fun showInstantReplacementCardAndInactive() {
        with(includeListOptions) {
            replacementCardDivider.visibility = VISIBLE
            replacementCardRelativeLayout.visibility = VISIBLE

            linkNewCardDivider.visibility = VISIBLE
            linkNewCardRelativeLayout.visibility = VISIBLE
        }
    }

    private fun showActivateVirtualTempCardRow(isTemporaryCardEnabled: Boolean) {
        with(includeListOptions) {
            activateVirtualTempCardDivider.visibility = VISIBLE
            activateVirtualTempCardRelativeLayout.visibility = VISIBLE

            if (isTemporaryCardEnabled){
                linkNewCardDivider.visibility = GONE
                linkNewCardRelativeLayout.visibility = GONE
            }else {
                linkNewCardDivider.visibility = VISIBLE
                linkNewCardRelativeLayout.visibility = VISIBLE
            }

        }
    }

    fun showCardNotReceivedDialog(fragment: Fragment?) {
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

}
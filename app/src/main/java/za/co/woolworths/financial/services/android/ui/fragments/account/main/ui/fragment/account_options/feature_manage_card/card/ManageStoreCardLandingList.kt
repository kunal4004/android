package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsManageCardListFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.util.FirebaseManager
import java.lang.Exception

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

    fun showListItem(storeCardFeatureType: Pair<StoreCardFeatureType?, Int?>) {
        CoroutineScope(Dispatchers.Main).launch {
            hideAllRows()
            when (val featureType = storeCardFeatureType.first) {

                is StoreCardFeatureType.ActivateVirtualTempCard ->
                    showActivateVirtualTempCardRow()

                is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive ->
                    showInstantReplacementCardAndInactive()

                is StoreCardFeatureType.StoreCardIsTemporaryFreeze ->
                    showStoreCardIsTemporaryFreeze(featureType)

                is StoreCardFeatureType.TemporaryCardEnabled ->
                    showTemporaryCardEnabled(featureType)

                StoreCardFeatureType.ManageMyCard -> showManageMyCardRow()

                else -> Unit

            }
        }
    }

    private fun showManageMyCardRow() {
        includeListOptions.manageCardDivider.visibility = VISIBLE
    }

    private fun showTemporaryCardEnabled(featureType: StoreCardFeatureType.TemporaryCardEnabled) {
        try {
        if (featureType.isBlockTypeNullInVirtualCardObject) {
            fragment?.setupGraph(
                R.navigation.account_options_manage_card_nav,
                R.id.payWithCardFragmentContainerView,
                R.id.payWithCardListFragment
            )
        }
        }catch (e : Exception){
          FirebaseManager.logException(e)
        }
        includeListOptions.payWithCardFragmentContainerView.visibility = VISIBLE
    }

    fun setupTemporaryCardGraph(){
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
                true -> {
                    cardFreezeViewModel.isSwitcherEnabled.value = true
                }
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

    private fun showActivateVirtualTempCardRow() {
        with(includeListOptions) {
            activateVirtualTempCardDivider.visibility = VISIBLE
            activateVirtualTempCardRelativeLayout.visibility = VISIBLE

            linkNewCardDivider.visibility = VISIBLE
            linkNewCardRelativeLayout.visibility = VISIBLE
        }
    }

}
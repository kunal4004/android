package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.DetermineCardToDisplay
import javax.inject.Inject

class ManageCardScreenSlidesAdapter @Inject constructor(fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    private var storeCardList: MutableList<StoreCard>? = mutableListOf()

    fun setItem(items: MutableList<StoreCard>?) = run {
        this.storeCardList = items ?: mutableListOf()
        notifyItemRangeInserted(0, itemCount)
    }

    override fun getItemCount(): Int = storeCardList?.size ?: 0

    override fun createFragment(position: Int): Fragment =
        when (storeCardList?.get(position)?.cardDisplay) {
            DetermineCardToDisplay.StoreCardIsInstantReplacementCardAndInactive -> InstantStoreCardReplacementCardFragment()
            else -> ScreenSlideFragment()
        }
}
package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import javax.inject.Inject

class ManageCardScreenSlidesAdapter @Inject constructor(fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    private var listOfStoreCards: MutableList<StoreCardFeatureType>? = mutableListOf()
    private var pageIds = mapOfPageIds()
    private fun isListOfItemsNullOrEmpty() = listOfStoreCards.isNullOrEmpty()

    private fun mapOfPageIds() = listOfStoreCards?.map { it.hashCode().toLong() }

    fun setItem(cardList: MutableList<StoreCardFeatureType>?) = run {
        this.listOfStoreCards?.clear()
        this.listOfStoreCards = cardList ?: mutableListOf()
        notifyItemChanged(0, itemCount)
        notifyItemRangeChanged(0, listOfStoreCards?.size ?: 1)
    }

    fun getListOfStoreCards(): MutableList<StoreCardFeatureType>? = this.listOfStoreCards

    override fun getItemCount(): Int =
        if (isListOfItemsNullOrEmpty()) 1
        else listOfStoreCards?.size ?: 1

    override fun createFragment(position: Int): Fragment {
        return when (isListOfItemsNullOrEmpty()) {
            true -> NoStoreCardFragment()
            else -> when (val card = listOfStoreCards?.get(position)) {
                is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive -> InstantStoreCardReplacementCardFragment.newInstance(card)
                is StoreCardFeatureType.StoreCardIsTemporaryFreeze -> FreezeUnFreezeStoreCardFragment.newInstance(storeCard = card)
                is StoreCardFeatureType.ActivateVirtualTempCard -> ActivateVirtualTempCardFragment()
                is StoreCardFeatureType.TemporaryCardEnabled -> TemporaryCardFragment()
                is StoreCardFeatureType.ManageMyCard -> NoStoreCardFragment()
                else -> NoStoreCardFragment()
            }
        }
    }

    override fun getItemId(position: Int): Long =
        if (isListOfItemsNullOrEmpty()) 0 else listOfStoreCards?.get(position)?.hashCode()?.toLong()
            ?: 0 // remove default fragment

    override fun containsItem(itemId: Long): Boolean =
        if (isListOfItemsNullOrEmpty()) false else pageIds?.contains(itemId) ?: false
}
package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
class ManageCardViewPagerAdapter(private var listOfStoreCards: MutableList<StoreCardFeatureType>? = mutableListOf(), fragment: FragmentActivity)
    : FragmentStateAdapter(fragment) {

    private var pageIds : List<Long>? = mapOfPageIds()
    private fun isListOfItemsNullOrEmpty() = listOfStoreCards.isNullOrEmpty()
    private fun mapOfPageIds()  = listOfStoreCards?.map { it.hashCode().toLong() }

    fun setItem(cardList: MutableList<StoreCardFeatureType>?) = run {
        this.listOfStoreCards?.clear()
        this.listOfStoreCards = cardList ?: mutableListOf()
        this.pageIds = mapOfPageIds()
        if (!isListOfItemsNullOrEmpty()) {
            this.notifyItemChanged(0, itemCount)
            this.notifyItemRangeChanged(0, itemCount)
        }
    }

    // Setting the automatically-generated ViewPager2's FrameLayout's clipChildren to false
    // Uncomment this block
    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        //(holder.itemView as ViewGroup).clipChildren = true
        holder.itemView.requestLayout()
        super.onBindViewHolder(holder, position, payloads)
    }
    fun getListOfStoreCards(): MutableList<StoreCardFeatureType>? = this.listOfStoreCards
    override fun getItemCount(): Int =
        if (isListOfItemsNullOrEmpty()) 1
        else listOfStoreCards?.size ?: 1
    override fun createFragment(position: Int): Fragment {
        return when (isListOfItemsNullOrEmpty()) {
            true -> NoStoreCardFragment()
            else -> when (val card = listOfStoreCards?.get(position)) {

                is StoreCardFeatureType.StoreCardFreezeCardUpShellMessage ->  StoreCardFreezeCardUpshellMessage.newInstance(storeCard = card)

                is StoreCardFeatureType.StoreCardActivateVirtualTempCardUpShellMessage ->  StoreCardActivateVTCUpshellMessageFragment.newInstance(storeCard = card)

                is StoreCardFeatureType.StoreCardIsActivateVirtualTempCardAndIsFreezeCard -> InstantStoreCardReplacementCardFragment.newInstance(
                    card
                )

                is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive -> InstantStoreCardReplacementCardFragment.newInstance(
                    card
                )
                is StoreCardFeatureType.StoreCardIsTemporaryFreeze -> FreezeUnFreezeStoreCardFragment.newInstance(
                    storeCard = card
                )
                is StoreCardFeatureType.ActivateVirtualTempCard -> ActivateVirtualTempCardFragment.newInstance(
                    storeCard = card
                )
                is StoreCardFeatureType.TemporaryCardEnabled -> TemporaryCardFragment.newInstance(
                    storeCard = card
                )
                is StoreCardFeatureType.ManageMyCard -> NoStoreCardFragment.newInstance(storeCard = card)
                else -> NoStoreCardFragment.newInstance(storeCard = card)
            }
        }
    }
    override fun getItemId(position: Int): Long =
        if (isListOfItemsNullOrEmpty()) 0 else listOfStoreCards?.get(position)?.hashCode()?.toLong()
            ?: 0 // remove default fragment

    override fun containsItem(itemId: Long): Boolean =
        if (isListOfItemsNullOrEmpty()) false else pageIds?.contains(itemId) ?: false

}
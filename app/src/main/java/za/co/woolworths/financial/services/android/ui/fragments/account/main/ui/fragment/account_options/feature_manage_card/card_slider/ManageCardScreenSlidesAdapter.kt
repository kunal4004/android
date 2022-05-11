package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import javax.inject.Inject

class ManageCardScreenSlidesAdapter @Inject constructor(fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    private var items: MutableList<StoreCard>? = mutableListOf()
    private var pageIds = mapOfPageIds()
    private fun isListOfItemsNullOrEmpty() = items.isNullOrEmpty()

    private fun mapOfPageIds() = items?.map { it.hashCode().toLong() }

    fun setItem(items: MutableList<StoreCard>?) = run {
        this.items?.clear()
        this.items = items ?: mutableListOf()
        notifyItemChanged(0, itemCount)
        notifyItemRangeChanged(0, items?.size ?: 1)
    }

    fun getListOfStoreCards(): MutableList<StoreCard>? = this.items

    override fun getItemCount(): Int =
        if (isListOfItemsNullOrEmpty()) 1
        else items?.size ?: 1

    override fun createFragment(position: Int): Fragment =
        if (isListOfItemsNullOrEmpty()) NoStoreCardFragment()
        else InstantStoreCardReplacementCardFragment.newInstance(items?.get(position))

    override fun getItemId(position: Int): Long =
        if (isListOfItemsNullOrEmpty()) 0 else items?.get(position)?.hashCode()?.toLong()
            ?: 0 // remove default fragment

    override fun containsItem(itemId: Long): Boolean =
        if (isListOfItemsNullOrEmpty()) false else pageIds?.contains(itemId) ?: false
    }
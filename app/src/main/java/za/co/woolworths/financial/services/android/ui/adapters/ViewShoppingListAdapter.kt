package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ItemShoppingListBinding
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import za.co.woolworths.financial.services.android.contracts.IShoppingList
import za.co.woolworths.financial.services.android.models.dto.ShoppingList

class ViewShoppingListAdapter(private var shopMutableList: MutableList<ShoppingList>, shoppingList: IShoppingList) : RecyclerSwipeAdapter<ViewShoppingListAdapter.ViewHolder>() {
    var onClickListener = shoppingList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemShoppingListBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onClickListener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shoppingList = shopMutableList[position]
        holder.bindItems(shoppingList)
    }

    class ViewHolder(val itemBinding: ItemShoppingListBinding, onClickListener: IShoppingList) : RecyclerView.ViewHolder(itemBinding.root) {
        var onClickListener = onClickListener
        fun bindItems(item: ShoppingList?) {
            item?.apply {
                // Returns a copy of this string having its first letter uppercased,
                // or the original string, if it's empty or already starts with an upper case letter.
                listName?.let { itemBinding?.tvListName?.text = it }
                itemBinding.tvListCount.text = ("$listCount  item").plus(if (listCount == 1) "" else "s")
                itemBinding.listItem.setOnClickListener { onClickListener.onShoppingListItemSelected(this) }
                itemBinding.tvDelete.setOnClickListener { onClickListener.onShoppingListItemDeleted(this, adapterPosition) }

            }
        }
    }

    override fun getItemCount() = shopMutableList.size

    override fun getSwipeLayoutResourceId(position: Int) = R.id.swipe

    fun getShoppingList(): MutableList<ShoppingList> = shopMutableList

    fun setShoppingList(shoppingList: MutableList<ShoppingList>) {
        shopMutableList = shoppingList
    }
    fun clear(){
        shopMutableList.clear()
        notifyDataSetChanged()
    }
}
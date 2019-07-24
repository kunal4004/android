package za.co.woolworths.financial.services.android.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import kotlinx.android.synthetic.main.item_shopping_list.view.*
import za.co.woolworths.financial.services.android.contracts.IShoppingList
import za.co.woolworths.financial.services.android.models.dto.ShoppingList

class ViewShoppingListAdapter(private var shopMutableList: MutableList<ShoppingList>, shoppingList: IShoppingList) : RecyclerSwipeAdapter<ViewShoppingListAdapter.ViewHolder>() {
    var onClickListener = shoppingList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping_list, parent, false)
        return ViewHolder(v, onClickListener)
    }

    override fun onBindViewHolder(holder: ViewShoppingListAdapter.ViewHolder, position: Int) {
        val shoppingList = shopMutableList[position]
        holder.bindItems(shoppingList)
    }

    class ViewHolder(itemView: View, onClickListener: IShoppingList) : RecyclerView.ViewHolder(itemView) {
        var onClickListener = onClickListener
        fun bindItems(item: ShoppingList?) {
            item?.apply {
                // Returns a copy of this string having its first letter uppercased,
                // or the original string, if it's empty or already starts with an upper case letter.
                listName?.let { itemView?.tvListName?.text = it }
                itemView.tvListCount.text = ("$listCount  item").plus(if (listCount == 1) "" else "s")
                itemView.listItem.setOnClickListener { onClickListener.onShoppingListItemSelected(this) }
                itemView.tvDelete.setOnClickListener { onClickListener.onShoppingListItemDeleted(this, adapterPosition) }

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
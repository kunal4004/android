package za.co.woolworths.financial.services.android.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_to_list_row.view.*
import za.co.woolworths.financial.services.android.models.dto.ShoppingList

class AddToShoppingListAdapter(private var shopMutableList: MutableList<ShoppingList>, private val clickListener: (ShoppingList) -> Unit) : RecyclerView.Adapter<AddToShoppingListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.add_to_list_row, parent, false)
        return ViewHolder(v, clickListener)
    }

    override fun onBindViewHolder(holder: AddToShoppingListAdapter.ViewHolder, position: Int) {
        val shoppingList = shopMutableList[position]
        holder.bindItems(shoppingList)
    }

    class ViewHolder(itemView: View, private val clickListener: (ShoppingList) -> Unit) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(shoppingList: ShoppingList) {
            shoppingList.apply {
                itemView.tvName.text = listName
                itemView.chxAddToList.isChecked = shoppingListRowWasSelected

                itemView.setOnClickListener {
                    shoppingListRowWasSelected = !shoppingListRowWasSelected
                    clickListener(this)
                }
            }
        }
    }

    override fun getItemCount() = shopMutableList.size

    fun getShoppingList(): MutableList<ShoppingList> = shopMutableList

    fun setShoppingList(mutableList: MutableList<ShoppingList>) {
        shopMutableList = mutableList
    }
}
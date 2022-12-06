package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.AddToListRowBinding
import za.co.woolworths.financial.services.android.models.dto.ShoppingList

class AddToShoppingListAdapter(private var shopMutableList: MutableList<ShoppingList>, private val clickListener: (ShoppingList) -> Unit) : RecyclerView.Adapter<AddToShoppingListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AddToListRowBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            clickListener
        )
    }

    override fun onBindViewHolder(holder: AddToShoppingListAdapter.ViewHolder, position: Int) {
        val shoppingList = shopMutableList[position]
        holder.bindItems(shoppingList)
    }

    class ViewHolder(val binding: AddToListRowBinding, private val clickListener: (ShoppingList) -> Unit) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(shoppingList: ShoppingList) {
            shoppingList.apply {
                binding.tvName.text = listName
                binding.chxAddToList.isChecked = shoppingListRowWasSelected

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
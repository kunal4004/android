package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.shopping_bags_radio_button.view.*
import za.co.woolworths.financial.services.android.models.dto.ShoppingBagsOptions

/**
 * Created by Kunal Uttarwar on 12/10/21.
 */
class ShoppingBagsRadioGroupAdapter(
    private var shoppingBagsOptionsList: List<ShoppingBagsOptions>?,
    private val listner: EventListner
) :
    RecyclerView.Adapter<ShoppingBagsRadioGroupAdapter.ShoppingBagsRadioGroupAdapterViewHolder>() {
    var checkedItemPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ShoppingBagsRadioGroupAdapter.ShoppingBagsRadioGroupAdapterViewHolder {
        return ShoppingBagsRadioGroupAdapterViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.shopping_bags_radio_button,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount(): Int {
        return shoppingBagsOptionsList?.size ?: 0
    }

    override fun onBindViewHolder(
        holder: ShoppingBagsRadioGroupAdapter.ShoppingBagsRadioGroupAdapterViewHolder,
        position: Int
    ) {
        holder.bindItem(position)
    }

    inner class ShoppingBagsRadioGroupAdapterViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindItem(position: Int) {
            itemView.apply {
                shoppingBagsOptionsList?.get(position)?.let { it ->
                    title.text = it?.title
                    subTitle.text = it?.description
                    radioSelector.isChecked = checkedItemPosition == position
                }
                setOnClickListener {
                    shoppingBagsOptionsList?.get(position)?.let {
                        listner.selectedShoppingBagType(it, position)
                    }
                    checkedItemPosition = position
                    notifyDataSetChanged()
                }
            }
        }
    }


    interface EventListner {
        fun selectedShoppingBagType(
            shoppingBagsOptionsList: ShoppingBagsOptions,
            position: Int
        )
    }
}
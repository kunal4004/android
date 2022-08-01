package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.shopping_bags_radio_button.view.*
import za.co.woolworths.financial.services.android.models.dto.app_config.native_checkout.ConfigShoppingBagsOptions
import za.co.woolworths.financial.services.android.ui.extension.bindColor

/**
 * Created by Kunal Uttarwar on 12/10/21.
 */
class ShoppingBagsRadioGroupAdapter(
    private var shoppingBagsOptionsList: List<ConfigShoppingBagsOptions>?,
    private val listener: EventListner,
    private val selectedShoppingBagType: Double?,
) :
    RecyclerView.Adapter<ShoppingBagsRadioGroupAdapter.ShoppingBagsRadioGroupAdapterViewHolder>() {
    var checkedItemPosition = -1

    init {
        // If there is a default shopping bags present set it selected
        shoppingBagsOptionsList?.forEach { shoppingBagsOptions ->
            if (shoppingBagsOptions.isDefault) {
                checkedItemPosition = shoppingBagsOptionsList?.indexOf(shoppingBagsOptions) ?: -1
                if (selectedShoppingBagType != null)
                    onItemClicked(selectedShoppingBagType.toInt() -1)
                else
                    onItemClicked(checkedItemPosition)
                return@forEach
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
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
        position: Int,
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

                    shoppingBagsSelectionLayout.setBackgroundColor(
                        if (radioSelector.isChecked) bindColor(R.color.selected_address_background_color) else bindColor(
                            R.color.white
                        )
                    )
                    title.setBackgroundColor(
                        if (radioSelector.isChecked) bindColor(R.color.selected_address_background_color) else bindColor(
                            R.color.white
                        )
                    )
                    subTitle.setBackgroundColor(
                        if (radioSelector.isChecked) bindColor(R.color.selected_address_background_color) else bindColor(
                            R.color.white
                        )
                    )
                }
                setOnClickListener {
                    onItemClicked(position)
                }
            }
        }
    }

    private fun onItemClicked(position: Int) {
        if (position < 0 || position >= itemCount) {
            return
        }
        shoppingBagsOptionsList?.get(position)?.let {
            listener.selectedShoppingBagType(it, position)
            notifyItemChanged(position, it)
        }
        // update last position as well
        val previousPosition = checkedItemPosition
        checkedItemPosition = position

        if (previousPosition < 0 || previousPosition >= itemCount) {
            return
        }
        shoppingBagsOptionsList?.get(previousPosition)?.let {
            notifyItemChanged(previousPosition, it)
        }
    }


    interface EventListner {
        fun selectedShoppingBagType(
            shoppingBagsOptionsList: ConfigShoppingBagsOptions,
            position: Int,
        )
    }
}
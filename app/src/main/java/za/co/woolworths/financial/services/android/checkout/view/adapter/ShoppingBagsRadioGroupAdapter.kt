package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ShoppingBagsRadioButtonBinding
import za.co.woolworths.financial.services.android.models.dto.app_config.native_checkout.ConfigShoppingBagsOptions

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
                    onItemClicked(selectedShoppingBagType.toInt() - 1)
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
            ShoppingBagsRadioButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ShoppingBagsRadioGroupAdapterViewHolder(val itemBinding: ShoppingBagsRadioButtonBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(position: Int) {
            itemBinding.apply {
                shoppingBagsOptionsList?.get(position)?.let { it ->
                    title?.text = it.title
                    subTitle?.text = it.description
                    radioSelector?.isChecked = checkedItemPosition == position

                    shoppingBagsSelectionLayout?.background = ContextCompat.getDrawable(
                        root.context,
                        if (radioSelector?.isChecked == true)
                            R.drawable.bg_shopping_bags_selected
                        else
                            R.drawable.bg_shopping_bags_unselected
                    )
                    subTitle?.visibility = if (radioSelector.isChecked) View.VISIBLE else View.GONE
                }
                root.setOnClickListener {
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
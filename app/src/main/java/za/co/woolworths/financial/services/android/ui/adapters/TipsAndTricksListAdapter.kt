package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.TipsAndTricksListItemBinding
import za.co.woolworths.financial.services.android.contracts.ITipsAndTricksListener

class TipsAndTricksListAdapter(val context: Activity, val listner: ITipsAndTricksListener) : RecyclerView.Adapter<TipsAndTricksListAdapter.ViewHolder>() {

    var icons: TypedArray = context.resources.obtainTypedArray(R.array.tips_tricks_list_item_icons)
    var names: Array<String> = context.resources.getStringArray(R.array.tips_tricks_item_names)

    override fun getItemCount(): Int {
        return names.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(names[position], icons.getResourceId(position, -1), listner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TipsAndTricksListItemBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    class ViewHolder(val itemBinding: TipsAndTricksListItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(name: String, icon: Int, listener: ITipsAndTricksListener) {
            itemBinding.name?.text = name
            itemBinding.listIcon?.setBackgroundResource(icon)
            itemBinding.root.setOnClickListener { listener.onListItemSelected(position) }
        }
    }

}
package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.res.TypedArray
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.tips_and_tricks_list_item.view.*
import za.co.woolworths.financial.services.android.ui.fragments.help.tipstricks.TipsAndTricksNavigator

class TipsAndTricksListAdapter(val context: Activity, val listner: TipsAndTricksNavigator) : RecyclerView.Adapter<TipsAndTricksListAdapter.ViewHolder>() {

    var icons: TypedArray = context.resources.obtainTypedArray(R.array.tips_tricks_list_item_icons)
    var names: Array<String> = context.resources.getStringArray(R.array.tips_tricks_item_names)

    override fun getItemCount(): Int {
        return names.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTipName?.text = names[position]
        holder.imgIcon?.setBackgroundResource(icons.getResourceId(position, -1))
        holder.container.setOnClickListener { listner.onListItemSelected(position) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.tips_and_tricks_list_item, parent, false))
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val tvTipName = view.name
        val imgIcon = view.listIcon
        val container = view
    }

}
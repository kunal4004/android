package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.Intent
import android.content.res.TypedArray
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.tips_and_tricks_list_item.view.*
import za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity

class TipsAndTricksListAdapter(val context: Activity) : RecyclerView.Adapter<TipsAndTricksListAdapter.ViewHolder>() {

    var icons: TypedArray = context.resources.obtainTypedArray(R.array.tips_tricks_list_item_icons)
    var names: Array<String> = context.resources.getStringArray(R.array.tips_tricks_item_names)

    override fun getItemCount(): Int {
        return names.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.tvTipName?.text = names[position]
        holder?.tvTipName?.setCompoundDrawablesRelativeWithIntrinsicBounds(icons.getResourceId(position, -1), 0, 0, 0)
        holder?.container?.setOnClickListener {openTipsAndTricksActivity(position)}
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.tips_and_tricks_list_item, parent, false))
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val tvTipName = view.name
        val container = view
    }

    fun openTipsAndTricksActivity(position: Int) {
        val intent = Intent(context, TipsAndTricksViewPagerActivity::class.java)
        intent.putExtra("position", position)
        context.startActivity(intent)
        context.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

}
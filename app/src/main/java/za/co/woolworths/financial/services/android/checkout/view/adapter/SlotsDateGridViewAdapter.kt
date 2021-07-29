package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.checkout.service.network.HeaderDate

/**
 * Created by Kunal Uttarwar on 22/07/21.
 */
class SlotsDateGridViewAdapter(
    context: Context, val resource: Int, deliveryGridList: List<HeaderDate>
) : ArrayAdapter<HeaderDate>(context, resource, deliveryGridList) {

    val deliveryGridList: List<HeaderDate> = deliveryGridList
    val contxt: Context = context

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convrtView = convertView
        val mHolder: GridViewHolder
        if (convrtView == null) {
            mHolder = GridViewHolder()
            convrtView =
                (contxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    resource,
                    null
                )
            mHolder.gridTitle =
                convrtView.findViewById<View>(R.id.gridTextView) as? TextView
            mHolder.gridSubtitle =
                convrtView.findViewById<View>(R.id.gridSubTextView) as? TextView
            convrtView.tag = mHolder
        } else {
            mHolder = convrtView.tag as GridViewHolder
        }
        mHolder.gridTitle?.text = deliveryGridList[position].dayInitial
        mHolder.gridSubtitle?.text = deliveryGridList[position].date
        return convrtView!!
    }

    override fun getCount(): Int {
        return deliveryGridList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): HeaderDate {
        return deliveryGridList[position]
    }

    internal class GridViewHolder {
        var gridTitle: TextView? = null
        var gridSubtitle: TextView? = null
    }
}
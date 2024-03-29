package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.checkout.service.network.HourSlots

/**
 * Created by Kunal Uttarwar on 22/07/21.
 */
class SlotsTimeGridViewAdapter(
    context: Context, val resource: Int, private val deliveryGridTitleList: List<HourSlots>
) : ArrayAdapter<HourSlots>(context, resource, deliveryGridTitleList) {
    private val contxt: Context = context

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
            convrtView.tag = mHolder
        } else {
            mHolder = convrtView.tag as GridViewHolder
        }
        mHolder.gridTitle?.text = deliveryGridTitleList[position].slot
        mHolder.gridTitle?.textSize = 11f
        mHolder.gridTitle?.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        return convrtView!!
    }

    override fun getCount(): Int {
        return deliveryGridTitleList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): HourSlots {
        return deliveryGridTitleList[position]
    }

    internal class GridViewHolder {
        var gridTitle: TextView? = null
    }
}
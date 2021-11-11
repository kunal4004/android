package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.checkout.view.ExpandableGrid
import za.co.woolworths.financial.services.android.checkout.viewmodel.DeliveryGridModel
import za.co.woolworths.financial.services.android.ui.extension.bindColor


/**
 * Created by Kunal Uttarwar on 20/07/21.
 */
class DeliverySlotsGridViewAdapter(
    context: Context, val resource: Int, private val deliveryGridModelList: ArrayList<DeliveryGridModel>
) : ArrayAdapter<DeliveryGridModel>(context, resource, deliveryGridModelList) {

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
                convrtView.findViewById<View>(R.id.cardTextView) as? TextView
            convrtView.tag = mHolder
        } else {
            mHolder = convrtView.tag as GridViewHolder
        }
        mHolder.gridTitle?.text = deliveryGridModelList[position].grid_title
        val backgroundColor = deliveryGridModelList[position].backgroundImgColor
        if (backgroundColor == ExpandableGrid.SlotGridColors.WHITE.color) {
            mHolder.gridTitle?.setBackgroundResource(R.drawable.rounded_grid_border_text_view)
        } else
            mHolder.gridTitle?.setBackgroundResource(R.drawable.rounded_grid_text_view)
        val drawable = mHolder.gridTitle?.background as? GradientDrawable
        drawable?.setColor(bindColor(backgroundColor))
        if (deliveryGridModelList[position].slot.selected == true)
            mHolder.gridTitle?.setTextColor(android.graphics.Color.WHITE)
        else
            mHolder.gridTitle?.setTextColor(bindColor(R.color.checkout_delivering_title))
        return convrtView!!
    }

    override fun getCount(): Int {
        return deliveryGridModelList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): DeliveryGridModel {
        return deliveryGridModelList[position]
    }

    internal class GridViewHolder {
        var gridTitle: TextView? = null
        var id: String = "0"
    }
}
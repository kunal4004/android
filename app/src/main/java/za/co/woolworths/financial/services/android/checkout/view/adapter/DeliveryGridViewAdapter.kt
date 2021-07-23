package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.checkout.viewmodel.DeliveryGridModel


/**
 * Created by Kunal Uttarwar on 20/07/21.
 */
class DeliveryGridViewAdapter(
    context: Context, resource: Int, deliveryGridModelList: ArrayList<DeliveryGridModel>
) : ArrayAdapter<DeliveryGridModel>(context, resource, deliveryGridModelList) {

    val resource: Int = resource
    val deliveryGridModelList: ArrayList<DeliveryGridModel> = deliveryGridModelList
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
            mHolder.grid_title =
                convrtView.findViewById<View>(R.id.cardTextView) as? TextView
            mHolder.cardLayout =
                convrtView.findViewById<View>(R.id.cardLayout) as? LinearLayout
            convrtView.tag = mHolder
        } else {
            mHolder = convrtView.tag as GridViewHolder
        }
        mHolder.grid_title?.text =  deliveryGridModelList[position].grid_title
        if (deliveryGridModelList[position].isSelected){
            mHolder.grid_title?.setTextColor(android.graphics.Color.WHITE)
            mHolder.cardLayout?.setBackgroundColor(android.graphics.Color.GREEN)
        }
        else
            mHolder.cardLayout?.setBackgroundColor(deliveryGridModelList[position].backgroundImgColor)
        return convrtView!!
    }

    override fun getCount(): Int {
        return deliveryGridModelList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): DeliveryGridModel? {
        return deliveryGridModelList[position]
    }

    internal class GridViewHolder {
        var grid_title: TextView? = null
        var cardLayout: LinearLayout? = null
        var id: Int = 0
    }
}

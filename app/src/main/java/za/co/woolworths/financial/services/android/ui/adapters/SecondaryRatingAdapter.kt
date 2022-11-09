package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.SecondaryRatings

class SecondaryRatingAdapter() : RecyclerView.Adapter<SecondaryRatingAdapter.ViewHolder>() {
    var dataList = emptyList<SecondaryRatings>()

    internal fun setDataList(dataList: List<SecondaryRatings>) {
        this.dataList = dataList
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSRHeader: TextView = itemView.findViewById(R.id.tvSRHeading)
        var tvSRValue: TextView = itemView.findViewById(R.id.tvSRValue)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_secondary_rating_row_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.tvSRHeader.text = data.label+":"
        holder.tvSRValue.text = data.value.toString()+"/"+data.valueRange
    }

    override fun getItemCount() = dataList.size
}
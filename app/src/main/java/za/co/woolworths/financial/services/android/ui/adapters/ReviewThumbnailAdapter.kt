package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.Thumbnail

class ReviewThumbnailAdapter(var context: Context, var thumbnailClickListener: ThumbnailClickListener) : RecyclerView.Adapter<ReviewThumbnailAdapter.ViewHolder>() {
    interface ThumbnailClickListener {
        fun thumbnailClicked()
    }
    var dataList = emptyList<Thumbnail>()

    internal fun setDataList(dataList: List<Thumbnail>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.image)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.review_thumbnail_row_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var data = dataList[position]
        holder.image.setImageResource(data.url)
        holder.image.setOnClickListener(View.OnClickListener {
            if (position == 2)
                thumbnailClickListener.thumbnailClicked()
        })
    }

    override fun getItemCount() = dataList.size
}
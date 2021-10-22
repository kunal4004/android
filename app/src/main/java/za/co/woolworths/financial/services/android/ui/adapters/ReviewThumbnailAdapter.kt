package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.review_thumbnail_row_item.view.*
import za.co.woolworths.financial.services.android.models.dto.Thumbnail

class ReviewThumbnailAdapter(var context: Context, var thumbnailClickListener: ThumbnailClickListener) : RecyclerView.Adapter<ReviewThumbnailAdapter.ViewHolder>() {
    interface ThumbnailClickListener {
        fun thumbnailClicked()
    }
    val defaultThumbnailDisplyed =3
    var dataList = emptyList<Thumbnail>()

    internal fun setDataList(dataList: List<Thumbnail>) {
        this.dataList = dataList
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.image)
        var flShowMore: FrameLayout = itemView.findViewById(R.id.flShowMore)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_thumbnail_row_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.image.setImageResource(data.url)
        if (position == (defaultThumbnailDisplyed-1) && itemCount==defaultThumbnailDisplyed)
            holder.flShowMore.visibility = VISIBLE
        holder.image.setOnClickListener(View.OnClickListener {
            if (position == (defaultThumbnailDisplyed-1)) {
                holder.flShowMore.visibility = GONE
                thumbnailClickListener.thumbnailClicked()
            }
        })
    }

    override fun getItemCount() = dataList.size
}
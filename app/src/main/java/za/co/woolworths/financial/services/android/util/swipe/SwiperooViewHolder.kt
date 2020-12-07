package za.co.woolworths.financial.services.android.util.swipe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R

abstract class SwiperooViewHolder<T>(itemView: View, context: Context?) : RecyclerView.ViewHolder(itemView) {

    var undoButton: Button
    var container: RelativeLayout

    interface Factory {
        fun createViewHolder(context: Context?, parent: ViewGroup?, viewType: Int): SwiperooViewHolder<*>?
    }

    abstract fun bindViewHolder(data: T)

    init {
        if (itemView !is RelativeLayout) {
            throw UnsupportedOperationException("Main layout must a Relative Layout")
        }
        container = itemView
        undoButton = LayoutInflater.from(context).inflate(R.layout.button_undo_swiperoo, container, false) as Button
        container.addView(undoButton)
    }
}
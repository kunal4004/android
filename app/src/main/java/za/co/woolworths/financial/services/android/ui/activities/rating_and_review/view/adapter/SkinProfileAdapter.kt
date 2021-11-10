package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.skin_profile_layout_cell.view.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.SkinProfile
import za.co.woolworths.financial.services.android.ui.extension.bindColor

class SkinProfileAdapter(private var skinProfileList: List<SkinProfile>):RecyclerView.Adapter<SkinProfileAdapter.ViewHolder>() {
     class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bindItems(skinProfile: SkinProfile) {
            itemView.txt_label.text = skinProfile.label + ":"
            itemView.txt_value.text = skinProfile.valueLabel
            if (skinProfile.colorCode != null) {
                itemView.cvSkinColor.setCardBackgroundColor(Color.parseColor(skinProfile.colorCode))
                itemView.cvSkinColor.visibility = View.VISIBLE
            }else
                itemView.cvSkinColor.visibility = View.GONE
        }
    }

    /*fun TextView.leftDrawable(@DrawableRes id: Int = 0, @DimenRes sizeRes: Int = 0, @ColorInt color: Int = 0, @ColorRes colorRes: Int = 0) {
        val drawable = (id)
        if (sizeRes != 0) {
            val size = resources.getDimensionPixelSize(sizeRes)
            drawable?.setBounds(0, 0, size, size)
        }
        if (color != 0) {
            drawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        } else if (colorRes != 0) {
            val colorInt = ContextCompat.getColor(context, colorRes)
            drawable?.setColorFilter(colorInt, PorterDuff.Mode.SRC_ATOP)
        }
        this.setCompoundDrawables(drawable, null, null, null)
    }
*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.skin_profile_layout_cell, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(skinProfileList[position])
    }

    override fun getItemCount(): Int {
        return skinProfileList.size
    }
}

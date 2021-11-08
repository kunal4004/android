package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.skin_profile_layout_cell.view.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.SkinProfile

class SkinProfileAdapter(private var skinProfileList: List<SkinProfile>):RecyclerView.Adapter<SkinProfileAdapter.ViewHolder>() {
     class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bindItems(skinProfile: SkinProfile) {
            itemView.txt_label.text = skinProfile.label + ":"
            itemView.txt_value.text = skinProfile.valueLabel
            if (skinProfile.colorCode != null)
                itemView.txt_value.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.selected_dot, 0);
            else
                itemView.txt_value.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

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

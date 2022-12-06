package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.SkinProfileLayoutCellBinding
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.SkinProfile

class SkinProfileAdapter(private var skinProfileList: List<SkinProfile>):RecyclerView.Adapter<SkinProfileAdapter.ViewHolder>() {
     class ViewHolder(val itemBinding: SkinProfileLayoutCellBinding): RecyclerView.ViewHolder(itemBinding.root) {

        fun bindItems(skinProfile: SkinProfile) {
            itemBinding.apply {
                txtLabel.text = skinProfile.label + ":"
                txtValue.text = skinProfile.valueLabel
                if (skinProfile.colorCode != null) {
                    cvSkinColor.setCardBackgroundColor(Color.parseColor(skinProfile.colorCode))
                    cvSkinColor.visibility = View.VISIBLE
                } else
                    cvSkinColor.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SkinProfileLayoutCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(skinProfileList[position])
    }

    override fun getItemCount(): Int {
        return skinProfileList.size
    }
}

package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide


import android.graphics.Color.parseColor
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.google.type.Color
import kotlinx.android.synthetic.main.pdp_rating_layout.*
import kotlinx.android.synthetic.main.review_detail_layout.*
import kotlinx.android.synthetic.main.review_detail_layout_cell.*
import kotlinx.android.synthetic.main.review_detail_layout_cell.view.*
import kotlinx.android.synthetic.main.reviews_skin_profile.*
import kotlinx.android.synthetic.main.reviews_skin_profile.skin_profile_layout
import kotlinx.android.synthetic.main.skin_profile_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class SkinProfileDialog(private val reviews: Reviews) : WBottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reviews_skin_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        close.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        setStaticValues()
        close_top?.setOnClickListener(this@SkinProfileDialog)
        close?.setOnClickListener(this@SkinProfileDialog)
    }

    private fun setStaticValues() {
        if(reviews.contextDataValue.isNotEmpty()) {
            reviews.contextDataValue[0].apply {
                skin_profile_layout.txt_layout_one.txt_label.text = label
                skin_profile_layout.txt_layout_one.txt_value.text = valueLabel
            }
        }

        if(reviews.tagDimensions.isNotEmpty() ){
            skin_profile_layout.txt_layout_two.txt_label.text = reviews.tagDimensions[0].label
            skin_profile_layout.txt_layout_two.txt_value.text = reviews.tagDimensions[0].valueLabel
            if(reviews.tagDimensions.size==1){
                skin_profile_layout.txt_layout_three.txt_label.text = reviews.tagDimensions[1].label
                skin_profile_layout.txt_layout_three.txt_value.text = reviews.tagDimensions[0].valueLabel
            }

        }
        img_view.visibility = View.VISIBLE

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.close_top -> dismiss()
            R.id.close -> dismiss()
        }
    }
}


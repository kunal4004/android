package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pdp_rating_layout.*
import kotlinx.android.synthetic.main.review_detail_layout.*
import kotlinx.android.synthetic.main.review_detail_layout_cell.*
import kotlinx.android.synthetic.main.review_detail_layout_cell.view.*
import kotlinx.android.synthetic.main.reviews_skin_profile.*
import kotlinx.android.synthetic.main.reviews_skin_profile.skin_profile_layout
import kotlinx.android.synthetic.main.skin_profile_layout.view.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class SkinProfileDialog : WBottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reviews_skin_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        close.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)
        setStaticValues()
        close_top?.setOnClickListener(this@SkinProfileDialog)
        close?.setOnClickListener(this@SkinProfileDialog)
    }

    private fun setStaticValues() {
        skin_profile_layout.txt_layout_one.txt_label.text = "Skin Shade"
        skin_profile_layout.txt_layout_two.txt_label.text = "Skin Type "
        skin_profile_layout.txt_layout_three.txt_label.text = "Skin Concern"

        skin_profile_layout.txt_layout_one.txt_value.text = "Pale Ivory"
        skin_profile_layout.txt_layout_two.txt_value.text = "Combination,Sensitive"
        skin_profile_layout.txt_layout_three.txt_value.text = "Aging, Dehydration"
        img_view.visibility = View.VISIBLE
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.close_top -> dismiss()
            R.id.close -> dismiss()
        }
    }
}


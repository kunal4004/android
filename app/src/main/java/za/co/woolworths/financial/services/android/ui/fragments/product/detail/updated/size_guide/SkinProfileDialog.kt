package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide

import android.graphics.Paint
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pdp_rating_layout.*
import kotlinx.android.synthetic.main.review_detail_layout.*
import kotlinx.android.synthetic.main.review_helpful_and_report_layout.*
import kotlinx.android.synthetic.main.skin_profile_layout_cell.*
import kotlinx.android.synthetic.main.skin_profile_layout_cell.view.*
import kotlinx.android.synthetic.main.reviews_skin_profile.*
import kotlinx.android.synthetic.main.reviews_skin_profile.skin_profile_layout
import kotlinx.android.synthetic.main.reviews_skin_profile.view.*
import kotlinx.android.synthetic.main.skin_profile_layout.view.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.SkinProfile
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.SkinProfileAdapter
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
        setDefaultUi(reviews)
        close_top?.setOnClickListener(this@SkinProfileDialog)
        close?.setOnClickListener(this@SkinProfileDialog)
    }

    private fun setDefaultUi(reviews: Reviews?) {
        reviews?.run {
            setSkinProfileLayout(contextDataValue, tagDimensions)
            skin_profile.text = Html.fromHtml("<b>"+userNickname+"'s</b> "+ getString(R.string.skin_profile))
        }
    }

    private fun setSkinProfileLayout(contextDataValue: List<SkinProfile>, tagDimensions: List<SkinProfile>) {
        if (contextDataValue.isNotEmpty() || tagDimensions.isNotEmpty()) {
            skin_profile_layout.rv_skin_profile.visibility = View.VISIBLE
        } else {
            skin_profile_layout.rv_skin_profile.visibility = View.GONE
        }

        skin_profile_layout.rv_skin_profile.layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false)
        val list: List<SkinProfile> = contextDataValue.plus(tagDimensions)
        skin_profile_layout.rv_skin_profile.adapter = SkinProfileAdapter(list)
        skin_profile_layout.rv_skin_profile.addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL))

        DividerItemDecoration(
                context,
                LinearLayoutManager.HORIZONTAL
        ).apply {
            skin_profile_layout.rv_skin_profile.rv_skin_profile.addItemDecoration(this)
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.close_top -> dismiss()
            R.id.close -> dismiss()
        }
    }
}


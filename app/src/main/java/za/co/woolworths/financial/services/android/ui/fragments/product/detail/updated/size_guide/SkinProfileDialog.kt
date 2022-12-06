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
import com.awfs.coordination.databinding.ReviewsSkinProfileBinding
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.SkinProfile
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.SkinProfileAdapter
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class SkinProfileDialog(private val reviews: Reviews) : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: ReviewsSkinProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ReviewsSkinProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            close.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setDefaultUi(reviews)
            closeTop?.setOnClickListener(this@SkinProfileDialog)
            close?.setOnClickListener(this@SkinProfileDialog)
        }
    }

    private fun setDefaultUi(reviews: Reviews?) {
        reviews?.run {
            setSkinProfileLayout(contextDataValue, tagDimensions)
            binding.skinProfile.text = Html.fromHtml("<b>"+userNickname+"'s</b> "+ getString(R.string.skin_profile))
        }
    }

    private fun setSkinProfileLayout(contextDataValue: List<SkinProfile>, tagDimensions: List<SkinProfile>) {
        binding.apply {
            if (contextDataValue.isNotEmpty() || tagDimensions.isNotEmpty()) {
                skinProfileLayout.rvSkinProfile.visibility = View.VISIBLE
            } else {
                skinProfileLayout.rvSkinProfile.visibility = View.GONE
            }

            skinProfileLayout.rvSkinProfile.layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
            val list: List<SkinProfile> = contextDataValue.plus(tagDimensions)
            skinProfileLayout.rvSkinProfile.adapter = SkinProfileAdapter(list)
            skinProfileLayout.rvSkinProfile.addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL)
            )

            DividerItemDecoration(
                context,
                LinearLayoutManager.HORIZONTAL
            ).apply {
                skinProfileLayout.rvSkinProfile.addItemDecoration(this)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.close_top -> dismiss()
            R.id.close -> dismiss()
        }
    }
}


package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.RatingsRatingdetailsBinding
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingDistribution
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingReviewResponse
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class RatingDetailDialog(private val ratingReviewData: RatingReviewResponse) :
    WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: RatingsRatingdetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = RatingsRatingdetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            pdpratings.ratingBarTop.visibility = View.VISIBLE
            pdpratings.tvTotalReviews.visibility = View.VISIBLE
            ratingReviewData.reviewStatistics.apply {
                pdpratings.ratingBarTop.rating = averageRating
                pdpratings.tvTotalReviews.text =
                    resources.getQuantityString(R.plurals.no_review, reviewCount, reviewCount)
                val recommend = recommendedPercentage.split("%")
                if (recommend.size == 2) {
                    tvRecommendPercent.text = "${recommend[0]}% "
                    tvRecommendTxtValue.text = recommend[1]
                }
                setRatingDistributionUI(ratingDistribution, reviewCount)
            }
            pdpratings.tvTotalReviews.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            close.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            closeTop?.setOnClickListener(this@RatingDetailDialog)
            close?.setOnClickListener(this@RatingDetailDialog)
        }
    }

    private fun setRatingDistributionUI(ratingDistribution: List<RatingDistribution>, reviewCount: Int){
        binding.apply {
            for (rating in ratingDistribution) {
                when (rating.ratingValue) {
                    1 -> {
                        progressbar1.progress =
                            Utils.calculatePercentage(rating.count, reviewCount)
                        tv1StarRatingCount.text = rating.count.toString()
                    }
                    2 -> {
                        progressbar2.progress =
                            Utils.calculatePercentage(rating.count, reviewCount)
                        tv2StarRatingCount.text = rating.count.toString()
                    }
                    3 -> {
                        progressbar3.progress =
                            Utils.calculatePercentage(rating.count, reviewCount)
                        tv3StarRatingCount.text = rating.count.toString()
                    }
                    4 -> {
                        progressbar4.progress =
                            Utils.calculatePercentage(rating.count, reviewCount)
                        tv4StarRatingCount.text = rating.count.toString()

                    }
                    5 -> {
                        progressbar5.progress =
                            Utils.calculatePercentage(rating.count, reviewCount)
                        tv5StarRatingCount.text = rating.count.toString()
                    }
                }
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
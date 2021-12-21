package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.ccs_end_session_dialog_fragment.*
import kotlinx.android.synthetic.main.pdp_rating_layout.*
import kotlinx.android.synthetic.main.product_details_options_and_information_layout.*
import kotlinx.android.synthetic.main.ratings_ratingdetails.*
import kotlinx.android.synthetic.main.ratings_ratingdetails.close
import kotlinx.android.synthetic.main.ratings_ratingdetails.close_top
import kotlinx.android.synthetic.main.reviews_skin_profile.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingDistribution
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingReviewResponse
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class RatingDetailDialog(private val ratingReviewData: RatingReviewResponse) :
    WBottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ratings_ratingdetails, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ratingBarTop.visibility = View.VISIBLE
        tvTotalReviews.visibility = View.VISIBLE
        ratingReviewData.reviewStatistics.apply {
            ratingBarTop.rating = averageRating
            tvTotalReviews.text =
                resources.getQuantityString(R.plurals.no_review, reviewCount, reviewCount)
            val recommend= recommendedPercentage.split("%")
            if (recommend.size == 2) {
                tvRecommendPercent.text = "${recommend[0]}% "
                tvRecommendTxtValue.text = recommend[1]
            }
            setRatingDistributionUI(ratingDistribution, reviewCount)
        }
        tvTotalReviews.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        close.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        close_top?.setOnClickListener(this@RatingDetailDialog)
        close?.setOnClickListener(this@RatingDetailDialog)
    }

    private fun setRatingDistributionUI(ratingDistribution: List<RatingDistribution>, reviewCount: Int){
        for (rating in ratingDistribution) {
            when (rating.ratingValue) {
                1 -> {
                    progressbar_1.progress =
                        Utils.calculatePercentage(rating.count, reviewCount)
                    tv_1_starRating_count.text = rating.count.toString()
                }
                2 -> {
                    progressbar_2.progress =
                        Utils.calculatePercentage(rating.count, reviewCount)
                    tv_2_starRating_count.text = rating.count.toString()
                }
                3 -> {
                    progressbar_3.progress =
                        Utils.calculatePercentage(rating.count, reviewCount)
                    tv_3_starRating_count.text = rating.count.toString()
                }
                4 -> {
                    progressbar_4.progress =
                        Utils.calculatePercentage(rating.count, reviewCount)
                    tv_4_starRating_count.text = rating.count.toString()

                }
                5 -> {
                    progressbar_5.progress =
                        Utils.calculatePercentage(rating.count, reviewCount)
                    tv_5_starRating_count.text = rating.count.toString()
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
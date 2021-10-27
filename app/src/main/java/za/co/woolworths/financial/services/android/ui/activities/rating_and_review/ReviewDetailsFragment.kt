package za.co.woolworths.financial.services.android.ui.activities.rating_and_review

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.review_detail_layout.*
import kotlinx.android.synthetic.main.review_detail_layout_cell.view.*
import kotlinx.android.synthetic.main.review_helpful_and_report_layout.*
import kotlinx.android.synthetic.main.skin_profile_layout.*
import kotlinx.android.synthetic.main.skin_profile_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Normal
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class ReviewDetailsFragment : Fragment() {

    private lateinit var productViewPagerAdapter: ProductReviewViewPagerAdapter

    companion object {
        fun newInstance() = ReviewDetailsFragment()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.review_detail_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.apply {
            val reviewData = Utils.jsonStringToObject(getString(KotlinUtils.REVIEW_DATA), Reviews::class.java) as Reviews
            setDefaultUi(reviewData)
            setProductImageViewPager(reviewData.photos.normal)
        }
    }

    private fun setDefaultUi(reviewData: Reviews?) {

        reviewData?.run {
            txt_date.text = submissionTime
            txt_reviewer_name.text = userNickname
            rating_bar.rating = rating
            tv_skin_label.text = title
            skin_detail.text = reviewText
            if(isVerifiedBuyer){
                txt_verified.visibility = View.VISIBLE
            } else {
                txt_verified.visibility = View.GONE
            }

            if (contextDataValue.isEmpty()) {
                skin_profile_layout.visibility = View.GONE
                txt_layout_one.visibility = View.GONE
            } else {
               skin_profile_layout.visibility = View.VISIBLE
               for ( contextItemData  in contextDataValue ){
                   skin_profile_layout.txt_layout_one.txt_label.text = contextItemData.label
                   skin_profile_layout.txt_layout_one.txt_value.text = contextItemData.valueLabel
               }
           }

          if (tagDimensions.isEmpty()) {
              skin_profile_layout.visibility = View.GONE
          } else {
              skin_profile_layout.visibility = View.VISIBLE
              skin_profile_layout.txt_layout_two.txt_label.text = tagDimensions.get(0).label
              skin_profile_layout.txt_layout_two.txt_value.text = tagDimensions.get(0).valueLabel
              skin_profile_layout.txt_layout_three.txt_label.text = tagDimensions.get(1).label
              skin_profile_layout.txt_layout_three.txt_value.text = tagDimensions.get(1).valueLabel
          }

          if (secondaryRatings.isEmpty()) {
              view_product_quality.visibility = View.GONE
          } else {
              view_product_quality.visibility = View.VISIBLE
            /*  if (secondaryRatings.get(0).displayType.equals("SLIDER")) {
                  view_product_quality.txtMinLabel.text = secondaryRatings.get(0).minLabel
                  view_product_quality.txtValueLabel.text = secondaryRatings.get(0).valueLabel
                  view_product_quality.txtMaxLabel.text = secondaryRatings.get(0).maxLabel
              } else {
                  view_product_quality.txt_product_quality_label.text = secondaryRatings.get(1).label
                  view_product_quality.txt_product_quality_value.text =
                          secondaryRatings.get(1).value.toString()
                                  .plus(resources.getString(R.string.slash))
                                  .plus(secondaryRatings.get(1).valueRange)
                  when (secondaryRatings.get(1).value) {
                       1 -> view_product_quality.progress_product_quality.btn_first_section.setBackgroundResource(R.drawable.bg_segmneted_progress_bar_selected)
                       2 -> view_product_quality.progress_product_quality.btn_second_section.setBackgroundResource(R.drawable.bg_segmneted_progress_bar_selected)
                       3 -> view_product_quality.progress_product_quality.btn_third_section.setBackgroundResource(R.drawable.bg_segmneted_progress_bar_selected)
                       4 -> view_product_quality.progress_product_quality.btn_third_section.setBackgroundResource(R.drawable.bg_segmneted_progress_bar_selected)
                       5 -> view_product_quality.progress_product_quality.btn_fourth_section.setBackgroundResource(R.drawable.bg_segmneted_progress_bar_selected)
                  }
              }
              if (secondaryRatings.get(1).displayType.equals("NORMAL")) {
                  view_product_quality.txtMinLabel.text = secondaryRatings.get(1).minLabel
                  view_product_quality.txtValueLabel.text = secondaryRatings.get(1).valueLabel
                  view_product_quality.txtMaxLabel.text = secondaryRatings.get(1).maxLabel
              } else {
                  view_product_quality.txt_product_quality_label.text = secondaryRatings.get(0).label
                  view_product_quality.txt_product_quality_value.text =
                          secondaryRatings.get(0).value.toString()
                                  .plus(resources.getString(R.string.slash))
                                  .plus(secondaryRatings.get(0).valueRange)
                  when (secondaryRatings.get(0).value) {
                      1 -> view_product_quality.progress_product_quality.btn_first_section.setBackgroundResource(R.drawable.bg_segmneted_progress_bar_selected)
                      2 -> view_product_quality.progress_product_quality.btn_second_section.setBackgroundResource(R.drawable.bg_segmneted_progress_bar_selected)
                      3 -> view_product_quality.progress_product_quality.btn_third_section.setBackgroundResource(R.drawable.bg_segmneted_progress_bar_selected)
                      4 -> view_product_quality.progress_product_quality.btn_third_section.setBackgroundResource(R.drawable.bg_segmneted_progress_bar_selected)
                      5 -> view_product_quality.progress_product_quality.btn_fourth_section.setBackgroundResource(R.drawable.bg_segmneted_progress_bar_selected)
                  }
              }
*/

          }
        }
        tvReport.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)
    }

    private fun setProductImageViewPager(photos: List<Normal>) {
       // val imageList = mutableListOf<String>()
      //  imageList.addAll(photos)
//        imageList.add("https://images.woolworthsstatic.co.za/Wool-Blend-Knit-Coat-9354530122857.jpg?V=jVvu&o=WN2gYxjfrI6XnwyYtIUD10B58Soj&")
//        imageList.add("https://images.woolworthsstatic.co.za/Wool-Blend-Knit-Coat-9354530122857.jpg?V=jVvu&o=WN2gYxjfrI6XnwyYtIUD10B58Soj&")
//        imageList.add("https://images.woolworthsstatic.co.za/Wool-Blend-Knit-Coat-9354530122857.jpg?V=jVvu&o=WN2gYxjfrI6XnwyYtIUD10B58Soj&")
//        imageList.add("https://images.woolworthsstatic.co.za/Wool-Blend-Knit-Coat-9354530122857.jpg?V=jVvu&o=WN2gYxjfrI6XnwyYtIUD10B58Soj&")
//        imageList.add("https://images.woolworthsstatic.co.za/Wool-Blend-Knit-Coat-9354530122857.jpg?V=jVvu&o=WN2gYxjfrI6XnwyYtIUD10B58Soj&")
//        imageList.add("https://images.woolworthsstatic.co.za/Wool-Blend-Knit-Coat-9354530122857.jpg?V=jVvu&o=WN2gYxjfrI6XnwyYtIUD10B58Soj&")
        activity?.apply {
            productViewPagerAdapter = ProductReviewViewPagerAdapter(context, photos)
                    .apply {
                        reviewProductImagesViewPager.let { pager ->
                            pager.adapter = this
                            tabDots.setupWithViewPager(pager, true)
                        }
                    }
        }
    }
}

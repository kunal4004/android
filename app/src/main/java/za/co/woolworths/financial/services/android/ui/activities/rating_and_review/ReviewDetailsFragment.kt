package za.co.woolworths.financial.services.android.ui.activities.rating_and_review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.review_detail_layout.*
import kotlinx.android.synthetic.main.review_detail_layout_cell.view.*
import kotlinx.android.synthetic.main.skin_profile_layout.view.*

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
        setDefaultUi()
        setProductImageViewPager()
    }

    private fun setDefaultUi() {

        skin_profile_layout.txt_layout_one.txt_label.text = getString(R.string.skin_shade)
        skin_profile_layout.txt_layout_two.txt_label.text = getString(R.string.skin_type)
        skin_profile_layout.txt_layout_three.txt_label.text = getString(R.string.skin_concerns)

        skin_profile_layout.txt_layout_two.img_view.visibility = View.VISIBLE

        txt_date.text = "18 Sep 2020"
        skin_profile_layout.txt_layout_one.txt_value.text = "Pale Ivory"
        skin_profile_layout.txt_layout_two.txt_value.text = "Pale Ivory"
        skin_profile_layout.txt_layout_three.txt_value.text = "Aging, Dehydration"
    }

    private fun setProductImageViewPager() {
        val imageList = mutableListOf<String>()
        imageList.add("https://images.woolworthsstatic.co.za/Wool-Blend-Knit-Coat-9354530122857.jpg?V=jVvu&o=WN2gYxjfrI6XnwyYtIUD10B58Soj&")
        imageList.add("https://images.woolworthsstatic.co.za/Wool-Blend-Knit-Coat-9354530122857.jpg?V=jVvu&o=WN2gYxjfrI6XnwyYtIUD10B58Soj&")
        imageList.add("https://images.woolworthsstatic.co.za/Wool-Blend-Knit-Coat-9354530122857.jpg?V=jVvu&o=WN2gYxjfrI6XnwyYtIUD10B58Soj&")
        imageList.add("https://images.woolworthsstatic.co.za/Wool-Blend-Knit-Coat-9354530122857.jpg?V=jVvu&o=WN2gYxjfrI6XnwyYtIUD10B58Soj&")
        imageList.add("https://images.woolworthsstatic.co.za/Wool-Blend-Knit-Coat-9354530122857.jpg?V=jVvu&o=WN2gYxjfrI6XnwyYtIUD10B58Soj&")
        imageList.add("https://images.woolworthsstatic.co.za/Wool-Blend-Knit-Coat-9354530122857.jpg?V=jVvu&o=WN2gYxjfrI6XnwyYtIUD10B58Soj&")
        activity?.apply {
            productViewPagerAdapter = ProductReviewViewPagerAdapter(context, imageList)
                .apply {
                    reviewProductImagesViewPager.let { pager ->
                        pager.adapter = this
                        tabDots.setupWithViewPager(pager,true)
                    }
                }
        }
    }
}

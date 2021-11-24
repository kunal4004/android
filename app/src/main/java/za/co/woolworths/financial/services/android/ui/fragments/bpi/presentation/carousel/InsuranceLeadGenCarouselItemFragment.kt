package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.carousel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.insurance_lead_gen_carousel_item.*
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.InsuranceLeadCarousel

class InsuranceLeadGenCarouselItemFragment : Fragment() {

    companion object {
        private val className = InsuranceLeadGenCarouselItemFragment::class.java.simpleName
        fun newInstance(carouselItem: Pair<String, InsuranceLeadCarousel>) =
            InsuranceLeadGenCarouselItemFragment().withArgs {
                putSerializable(className, carouselItem)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.insurance_lead_gen_carousel_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val carouselItem = arguments?.get(className) as? Pair<*, *>
        (carouselItem?.second as? InsuranceLeadCarousel)?.apply {
            insuranceLeadGenCarouselImageview?.setImageResource(imageResource)
            insuranceLeadGenCarouselTitleTextView?.text = bindString(titleResource)
            insuranceLeadGenCarouselDescTextView?.text = bindString(descriptionResource)
        }
    }

}
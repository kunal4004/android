package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.carousel

import android.os.Bundle
import android.view.View
import com.awfs.coordination.databinding.InsuranceLeadGenCarouselItemBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.InsuranceLeadCarousel
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class InsuranceLeadGenCarouselItemFragment : BaseFragmentBinding<InsuranceLeadGenCarouselItemBinding>(InsuranceLeadGenCarouselItemBinding::inflate) {

    companion object {
        private val className = InsuranceLeadGenCarouselItemFragment::class.java.simpleName
        fun newInstance(carouselItem: Pair<String, InsuranceLeadCarousel>) =
            InsuranceLeadGenCarouselItemFragment().withArgs {
                putSerializable(className, carouselItem)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            val carouselItem = arguments?.get(className) as? Pair<*, *>
            (carouselItem?.second as? InsuranceLeadCarousel)?.apply {
                insuranceLeadGenCarouselImageview?.setImageResource(imageResource)
                insuranceLeadGenCarouselTitleTextView?.text = title
                insuranceLeadGenCarouselDescTextView?.text = description
            }
        }
    }

}
package za.co.woolworths.financial.services.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.on_boarding_fragment.*
import za.co.woolworths.financial.services.android.models.dto.OnBoardingModel
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class OnBoardingFragment : Fragment() {

    companion object {
        private const val ON_BOARDING_MODEL = "ON_BOARDING_MODEL"
        fun newInstance(onBoardingModel: OnBoardingModel) = OnBoardingFragment().withArgs {
            putSerializable(ON_BOARDING_MODEL, onBoardingModel)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.on_boarding_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val onBoardingModel = arguments?.get(ON_BOARDING_MODEL) as? OnBoardingModel
        onBoardingModel?.apply {
            onboardingCardsImageView?.setImageResource(image_drawable_id)
            onBoardingItemTitleTextView?.text = activity?.resources?.getString(title)
        }
    }
}
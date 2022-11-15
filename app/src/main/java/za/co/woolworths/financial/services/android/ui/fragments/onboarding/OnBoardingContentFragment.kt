package za.co.woolworths.financial.services.android.ui.fragments.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.OnBoardingContentFragmentBinding
import za.co.woolworths.financial.services.android.models.dto.OnBoardingModel
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType

class OnBoardingContentFragment : BaseFragmentBinding<OnBoardingContentFragmentBinding>(OnBoardingContentFragmentBinding::inflate) {

    companion object {
        private const val ON_BOARDING_MODEL = "ON_BOARDING_MODEL"
        fun newInstance(onBoardingModel: Pair<OnBoardingScreenType, OnBoardingModel>) = OnBoardingContentFragment().withArgs {
            putSerializable(ON_BOARDING_MODEL, onBoardingModel)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.on_boarding_content_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            val onBoardingModel = arguments?.get(ON_BOARDING_MODEL) as? Pair<*, *>
            (onBoardingModel?.second as? OnBoardingModel)?.apply {
                when (onBoardingModel.first) {
                    OnBoardingScreenType.START_UP -> {
                        singleImageView?.setImageResource(cardImageId)
                        onBoardingItemTitleTextView?.text = activity?.resources?.getString(title)
                    }
                    OnBoardingScreenType.ACCOUNT -> {
                        singleImageView?.setImageResource(cardImageId)
                        singleImageView?.visibility = VISIBLE
                        onBoardingItemTitleTextView?.text = activity?.resources?.getString(title)
                        singleImageView?.setImageResource(cardImageId)
                    }
                }
            }
        }
    }
}
package za.co.woolworths.financial.services.android.ui.fragments.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.on_boarding_content_fragment.*
import za.co.woolworths.financial.services.android.models.dto.OnBoardingModel
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType

class OnBoardingContentFragment : Fragment() {

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
        val onBoardingModel = arguments?.get(ON_BOARDING_MODEL) as? Pair<*, *>
        (onBoardingModel?.second as? OnBoardingModel)?.apply {
            when (onBoardingModel.first) {
                OnBoardingScreenType.START_UP -> {
                    onBoardingImageView?.setImageResource(cardImageId)
                    onBoardingImageView?.visibility = VISIBLE
                    singleImageView?.visibility = GONE
                    onBoardingItemTitleTextView?.text = activity?.resources?.getString(title)
                    imageBackgroundView?.setBackgroundResource(colorId)
                }
                OnBoardingScreenType.ACCOUNT -> {
                    singleImageView?.setImageResource(cardImageId)
                    onBoardingImageView?.visibility = GONE
                    singleImageView?.visibility = VISIBLE
                    onBoardingItemTitleTextView?.text = activity?.resources?.getString(title)
                    singleImageView?.setImageResource(cardImageId)
                }
            }
        }
    }
}
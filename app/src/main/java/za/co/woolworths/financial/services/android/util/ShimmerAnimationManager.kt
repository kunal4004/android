package za.co.woolworths.financial.services.android.util

import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout

class ShimmerAnimationManager {

    companion object {
        fun initShimmer(view: ShimmerFrameLayout?) {
            val shimmer = Shimmer.AlphaHighlightBuilder().build()
            view?.setShimmer(shimmer)
            view?.isEnabled = true
        }

        fun startProgress(view: ShimmerFrameLayout?) {
            view?.startShimmer()
            view?.isEnabled = false
        }

        fun stopProgress(view: ShimmerFrameLayout?) {
            view?.setShimmer(null)
            view?.stopShimmer()
            view?.isEnabled = true
        }
    }
}
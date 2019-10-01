package za.co.woolworths.financial.services.android.ui.views

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.awfs.coordination.R
import com.skydoves.balloon.*

class AddedToCartBalloonFactory : Balloon.Factory() {
    override fun create(context: Context, lifecycle: LifecycleOwner): Balloon {
        return createBalloon(context) {
            setLayout(R.layout.add_to_cart_toast_layout)
            setArrowSize(0)
            setArrowOrientation(ArrowOrientation.BOTTOM)
            setArrowPosition(0.5f)
            setHeight(60)
            setWidthRatio(0.95f)
            setCornerRadius(0f)
            setBackgroundColor(ContextCompat.getColor(context, R.color.toast_background))
            setBalloonAnimation(BalloonAnimation.CIRCULAR)
            setLifecycleOwner(lifecycle)
        }
    }
}
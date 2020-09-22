package za.co.woolworths.financial.services.android.ui.views.snackbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.awfs.coordination.R
import com.google.android.material.snackbar.ContentViewCallback

class OneAppSnackbarView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ContentViewCallback {

    private val toastMessageConstraintLayout: ConstraintLayout

    init {
        View.inflate(context, R.layout.view_snackbar_item, this)
        clipToPadding = false
        this.toastMessageConstraintLayout = findViewById(R.id.toastMessageConstraintLayout)
    }

    override fun animateContentIn(delay: Int, duration: Int) {
//        val scaleX = ObjectAnimator.ofFloat(toastMessageConstraintLayout, View.SCALE_X, 0f, 1f)
//        val scaleY = ObjectAnimator.ofFloat(toastMessageConstraintLayout, View.SCALE_Y, 0f, 1f)
//        val animatorSet = AnimatorSet().apply {
//            interpolator = OvershootInterpolator()
//            setDuration(3000)
//            playTogether(scaleX, scaleY)
//        }
//        animatorSet.start()
    }

    override fun animateContentOut(delay: Int, duration: Int) {
    }
}
package za.co.woolworths.financial.services.android.ui.activities.write_a_review.view

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRatingBar
import com.awfs.coordination.R

class CustomRatingBar(context: Context, attrs: AttributeSet) : AppCompatRatingBar(context, attrs) {

    companion object {
        var clicked = false
    }

    fun drawBoundingBox() {
        //Refresh the view by calling onDraw function
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        val starSize = width / numStars
        val drawable = progressDrawable

        if (drawable != null) {
            for (i in 0 until numStars) {
                val starDrawable = if (i < rating) {
                    clicked = false
                    getFilledStarDrawable()
                } else {
                    if (clicked) {
                        getFilledErrorStarDrawable()
                    } else getEmptyStarDrawable()
                }
                val x = i * starSize
                starDrawable.setBounds(x, 0, x + starSize, height)
                canvas?.let { starDrawable.draw(it) }
            }
        }
    }

    private fun getEmptyStarDrawable(): Drawable {
        return resources.getDrawable(R.drawable.write_a_review_blank_ratingbar, null)
    }

    private fun getFilledErrorStarDrawable(): Drawable {
        val drawable = resources.getDrawable(R.drawable.write_a_review_blank_ratingbar, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter =
                BlendModeColorFilter(resources.getColor(R.color.red), BlendMode.SRC_ATOP)
        } else {
            drawable.setColorFilter(resources.getColor(R.color.red), PorterDuff.Mode.SRC_ATOP)
        }
        return drawable
    }

    private fun getFilledStarDrawable(): Drawable {
        val drawable = resources.getDrawable(R.drawable.write_a_review_fill_ratingbar, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter =
                BlendModeColorFilter(resources.getColor(R.color.black), BlendMode.SRC_ATOP)
        } else {
            drawable.setColorFilter(resources.getColor(R.color.black), PorterDuff.Mode.SRC_ATOP)
        }
        return drawable

    }
}

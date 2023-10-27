package za.co.woolworths.financial.services.android.ui.views.tooltip

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance



class CustomText(typeface: Typeface?, textColor: Int) : CharacterStyle(), UpdateAppearance {

    private val typeface: Typeface
    private val textColor: Int
    init {
        this.typeface = typeface!!
        this.textColor = textColor
    }
    override fun updateDrawState(ds: TextPaint) {
        ds.typeface = typeface
        ds.color = textColor
    }


}

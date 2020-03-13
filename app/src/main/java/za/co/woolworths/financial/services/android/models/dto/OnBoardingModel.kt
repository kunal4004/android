package za.co.woolworths.financial.services.android.models.dto

import com.awfs.coordination.R
import java.io.Serializable

data class OnBoardingModel(val title: Int, val cardImageId: Int, val colorId: Int = R.color.white) : Serializable

package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.schema

import androidx.compose.ui.graphics.Color
import za.co.woolworths.financial.services.android.ui.wfs.theme.Obsidian
import za.co.woolworths.financial.services.android.ui.wfs.theme.White
import javax.annotation.concurrent.Immutable

enum class OfferViewColors {
    Light,
    Dark;
    fun color(): OfferItemViewColor {
        return when (this) {
            Light -> OfferItemViewColor(
                titleColor = White,
                descriptionColor = White,
                buttonTextColor = Obsidian,
                buttonBackgroundColor = White
            )
            Dark -> OfferItemViewColor(
                titleColor = Obsidian,
                descriptionColor = Obsidian,
                buttonTextColor = White,
                buttonBackgroundColor = Obsidian
            )
        }
    }
}

@Immutable
data class OfferItemViewColor(
    var titleColor: Color,
    var descriptionColor: Color,
    var buttonTextColor: Color,
    var buttonBackgroundColor: Color,
)

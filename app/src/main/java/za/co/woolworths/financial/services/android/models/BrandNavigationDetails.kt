package za.co.woolworths.financial.services.android.models

import java.io.Serializable

data class BrandNavigationDetails(
    var brandText: String?,
    var range: String?,
    var displayName: String?,
    var navigationState: String?,
    var filterContent: Boolean,
    var isBrandLandingPage: Boolean,
    var isComingFromBLP: Boolean
) : Serializable
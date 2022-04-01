package za.co.woolworths.financial.services.android.models

import java.io.Serializable

data class BrandNavigationDetails(
    var brandText: String? = "",
    var range: String? = "",
    var displayName: String? = "",
    var navigationState: String? = "",
    var bannerImage: String? = "",
    var bannerLabel: String? = "",
    var filterContent: Boolean = false,
    var isBrandLandingPage: Boolean = false,
    var isComingFromBLP: Boolean = false
) : Serializable {

    constructor(
        brandText: String?,
        displayName: String?,
        isBrandLandingPage: Boolean,
        filterContent: Boolean
    ) : this(
        brandText, null, displayName, null, null, null,
        filterContent, isBrandLandingPage, false
    )

    constructor(
        brandText: String?,
        bannerLabel: String?,
        bannerImage: String?
    ) : this(
        brandText, null, null, null, bannerImage, bannerLabel,
        false, false, false
    )

    constructor(
        brandText: String?,
        navigationState: String?
    ) : this(
        brandText, null, null, navigationState, null, null,
        false, false, false
    )

    constructor(
        brandText: String?,
        displayName: String?,
        navigationState: String?,
        bannerImage: String?,
        bannerLabel: String?,
        isComingFromBLP: Boolean,
        filterContent: Boolean
    ) : this(
        brandText, null, displayName, navigationState, bannerImage, bannerLabel,
        filterContent, false, isComingFromBLP
    )
}
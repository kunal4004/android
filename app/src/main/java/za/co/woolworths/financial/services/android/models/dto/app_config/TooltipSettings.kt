package za.co.woolworths.financial.services.android.models.dto.app_config

import kotlinx.android.parcel.Parcelize

@Parcelize
data class TooltipSettings  (
    val isAutoDismissEnabled: Boolean,
    var autoDismissDuration: Long,
)
